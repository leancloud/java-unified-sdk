package cn.leancloud.push.lite.ws;

import android.content.Context;
import android.util.Log;

import org.java_websocket.framing.CloseFrame;
import com.alibaba.fastjson.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import cn.leancloud.push.lite.AVCallback;
import cn.leancloud.push.lite.AVException;
import cn.leancloud.push.lite.AVInstallation;
import cn.leancloud.push.lite.AVOSCloud;
import cn.leancloud.push.lite.proto.CommandPacket;
import cn.leancloud.push.lite.proto.LoginPacket;
import cn.leancloud.push.lite.proto.Messages;
import cn.leancloud.push.lite.rest.AVHttpClient;
import cn.leancloud.push.lite.utils.PacketAssembler;
import cn.leancloud.push.lite.utils.StringUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AVConnectionManager implements WebSocketClientMonitor {
  private static final String TAG = AVConnectionManager.class.getSimpleName();

  private static AVConnectionManager instance = null;
  private AVStandardWebSocketClient webSocketClient = null;
  private Object webSocketClientWatcher = new Object();
  private String currentRTMConnectionServer = null;
  private int retryConnectionCount = 0;
  private boolean connectionEstablished = false;

  private volatile boolean connecting = false;
  private volatile AVCallback pendingCallback = null;
  private Context context;
  private Map<String, AVConnectionListener> connectionListeners = new ConcurrentHashMap<>(1);

  public synchronized static AVConnectionManager getInstance(Context context) {
    if (instance == null) {
      instance = new AVConnectionManager(context, false);
    }
    return instance;
  }

  private AVConnectionManager(Context context, boolean autoConnection) {
    this.context = context;
    connectionListeners.put(AVPushMessageListener.DEFAULT_ID, AVPushMessageListener.getInstance());
    if (autoConnection) {
      startConnection();
    }
  }

  private void resetConnectingStatus(boolean succeed) {
    this.connecting = false;
    if (null != pendingCallback) {
      if (succeed) {
        pendingCallback.internalDone(null);
      } else {
        pendingCallback.internalDone(new AVException(AVException.TIMEOUT, "network timeout."));
      }
    }
    pendingCallback = null;
  }

  private void reConnectionRTMServer() {
    retryConnectionCount++;
    if (retryConnectionCount <= 3) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            long sleepMS = (long)Math.pow(2, retryConnectionCount) * 1000;
            Thread.sleep(sleepMS);
            if (AVOSCloud.isDebugLogEnabled()) {
              Log.d(TAG, "reConnect rtm server. count=" + retryConnectionCount);
            }
            startConnectionInternal();
          } catch (InterruptedException ex) {
            Log.w(TAG,"failed to start connection.", ex);
          }
        }
      }).start();
    } else {
      Log.e(TAG, "have tried " + (retryConnectionCount - 1) + " times, stop connecting...");
      resetConnectingStatus(false);
    }
  }

  private String updateTargetServer(JSONObject rtmConnectionServerResponse) {
    String primaryServer = rtmConnectionServerResponse.getString("server");
    String secondary = rtmConnectionServerResponse.getString("secondary");
    if (StringUtil.isEmpty(this.currentRTMConnectionServer) || this.currentRTMConnectionServer.equalsIgnoreCase(secondary)) {
      this.currentRTMConnectionServer = primaryServer;
    } else {
      this.currentRTMConnectionServer = secondary;
    }
    return this.currentRTMConnectionServer;
  }

  private void initWebSocketClient(String targetServer) {
    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "try to connect server: " + targetServer);
    }

    SSLSocketFactory sf = null;
    try {
      SSLContext sslContext = SSLContext.getDefault();
      sf = sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException exception) {
      Log.e(TAG, "failed to get SSLContext, cause: " + exception.getMessage());
    }
    URI targetURI;
    try {
      targetURI = URI.create(targetServer);
    } catch (Exception ex) {
      Log.e(TAG, "failed to parse targetServer:" + targetServer + ", cause:" + ex.getMessage());
      targetURI = null;
    }
    if (null == targetURI) {
      return;
    }

    synchronized (webSocketClientWatcher) {
      if (null != webSocketClient) {
        try {
          webSocketClient.close();
        } catch (Exception ex) {
          Log.e(TAG, "failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
      int connectTimeout = 30000;// milliseconds
      webSocketClient = new AVStandardWebSocketClient(targetURI, AVStandardWebSocketClient.SUB_PROTOCOL_2_3,
          true, true, sf, connectTimeout, AVConnectionManager.this);
      webSocketClient.connect();
    }
  }

  public void startConnection(AVCallback callback) {
    if (this.connectionEstablished) {
      if (null != callback) {
        callback.internalDone(null);
      }
    } else if (this.connecting) {
      if (null != callback) {
        this.pendingCallback = callback;
      }
    } else {
      this.connecting = true;
      this.pendingCallback = callback;
      startConnectionInternal();
    }
  }

  public void startConnection() {
    startConnection(null);
  }

  private void startConnectionInternal() {
    final String appId = AVOSCloud.applicationId;
    final String installationId = AVInstallation.getCurrentInstallation(this.context).getInstallationId();

    AVHttpClient.getInstance().fetchPushWSServer(appId, installationId, 1, new Callback<JSONObject>() {
      public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
        JSONObject result = response.body();
        if (null != result) {
          String targetServer = updateTargetServer(result);
          initWebSocketClient(targetServer);
        }
      }

      public void onFailure(Call<JSONObject> call, Throwable t) {
        Log.w(TAG, "failed to fetch WebSocket Server.", t);
        reConnectionRTMServer();
      }
    });
  }

  public void cleanup() {
    resetConnection();

    this.connectionListeners.clear();
    this.pendingCallback = null;
  }

  void resetConnection() {
    synchronized (webSocketClientWatcher) {
      if (null != webSocketClient) {
        try {
          webSocketClient.closeConnection(CloseFrame.ABNORMAL_CLOSE, "Connectivity broken");
        } catch (Exception ex) {
          Log.e(TAG, "failed to close websocket client.", ex);
        } finally {
          webSocketClient = null;
        }
      }
    }

    connectionEstablished = false;
    retryConnectionCount = 0;
    connecting = false;
  }

  public void sendPacket(CommandPacket packet) {
    this.webSocketClient.send(packet);
  }

  public boolean isConnectionEstablished() {
    return this.connectionEstablished;
  }

  /**
   * WebSocketClientMonitor interfaces
   */
  public void onOpen() {
    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "webSocket established...");
    }
    connectionEstablished = true;
    retryConnectionCount = 0;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketOpen();
    }

    resetConnectingStatus(true);

    // auto send login packet.
    LoginPacket lp = new LoginPacket();
    lp.setAppId(AVOSCloud.getApplicationId());
    lp.setInstallationId(AVInstallation.getCurrentInstallation(this.context).getInstallationId());
    this.sendPacket(lp);
  }

  public void onClose(int var1, String var2, boolean var3) {
    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "webSocket closed...");
    }
    connectionEstablished = false;
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onWebSocketClose();
    }
  }

  public void onMessage(ByteBuffer bytes) {
    Messages.GenericCommand command = PacketAssembler.getInstance().disassemblePacket(bytes);
    if (null == command) {
      return;
    }

    if (AVOSCloud.isDebugLogEnabled()) {
      Log.d(TAG, "downlink: " + command.toString());
    }

    String peerId = command.getPeerId();
    if (StringUtil.isEmpty(peerId)) {
      peerId = AVPushMessageListener.DEFAULT_ID;
    }
    Integer requestKey = command.hasI() ? command.getI() : null;
    AVConnectionListener listener = this.connectionListeners.get(peerId);
    if (null != listener) {
      listener.onMessageArriving(peerId, requestKey, command);
    } else {
      Log.w(TAG, "no peer subscribed message, ignore it. peerId=" + peerId + ", requestKey=" + requestKey);
    }
  }

  public void onError(Exception exception) {
    connectionEstablished = false;
    reConnectionRTMServer();
    for (AVConnectionListener listener: connectionListeners.values()) {
      listener.onError(null, null);
    }
  }
}
