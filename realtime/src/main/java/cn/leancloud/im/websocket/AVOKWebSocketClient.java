package cn.leancloud.im.websocket;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;
import okhttp3.*;
import okio.ByteString;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * useage:
 *   AVOKWebSocketClient client = new AVOKWebSocketClient(null, true);
 *   client.connect(wsUrl);
 *   client.sendMessage("hello world");
 *   client.close();
 *
 */
public class AVOKWebSocketClient {
  private static AVLogger LOGGER = LogUtil.getLogger(AVOKWebSocketClient.class);
  private final static int RECONNECT_INTERVAL = 10 * 1000;    //重连自增步长
  private final static long RECONNECT_MAX_TIME = 120 * 1000;   //最大重连间隔

  private OkHttpClient client = null;
  private Request request = null;
  private WebSocket webSocket = null;
  private Status currentStatus = Status.DISCONNECTED;
  private int reconnectCount = 0;
  private boolean isManualClose = false;
  private boolean isNeedReconnect;          //是否需要断线自动重连
  private Lock lock = new ReentrantLock();
  private WsStatusListener wsStatusListener = null;
  private Timer reconnectTimer = new Timer(true);

  private WebSocketListener internalSocketListener = new WebSocketListener() {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
      LOGGER.d("onOpen");
      AVOKWebSocketClient.this.webSocket = webSocket;
      AVOKWebSocketClient.this.currentStatus = Status.CONNECTED;
      connected();
      if (null != wsStatusListener) {
        wsStatusListener.onOpen(response);
      }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      LOGGER.d("onMessage");
      if (null != wsStatusListener) {
        wsStatusListener.onMessage(text);
      }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
      LOGGER.d("onMessage");
      if (null != wsStatusListener) {
        wsStatusListener.onMessage(bytes);
      }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
      LOGGER.d("onClosing");
      if (null != wsStatusListener) {
        wsStatusListener.onClosing(code, reason);
      }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
      LOGGER.d("onClosed");
      if (null != wsStatusListener) {
        wsStatusListener.onClosed(code, reason);
      }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
      if (isManualClose) {
        return;
      }
      tryReconnect();
      // maybe response is null.
      LOGGER.w("onFailure", t);
      if (null != wsStatusListener) {
        wsStatusListener.onFailure(t, response);
      }
    }
  };

  public enum Status {
    DISCONNECTED, CONNECTED, CONNECTING, CLOSING, RECONNECT;
  }

  public AVOKWebSocketClient(WsStatusListener externalListener, boolean needReconnect) {
    this.wsStatusListener = externalListener;
    this.isNeedReconnect = needReconnect;
    this.client = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
  }

  public Status currentStatus() {
    return this.currentStatus;
  }

  public boolean sendMessage(String msg) {
    return false;
  }

  public boolean sendMessage(ByteString byteString) {
    return false;
  }

  public void connect(String wsUrl) {
    //构造request对象
    request = new Request.Builder()
            .url(wsUrl)
            .build();
    isManualClose = false;
    buildConnect();
  }

  public void close() {
    isManualClose = true;
    if (Status.CONNECTED == currentStatus && null != this.webSocket) {
      cancelReconnect();
      if (null != this.client) {
        this.client.dispatcher().cancelAll();
      }
      boolean isClosed = webSocket.close(CODE.NORMAL_CLOSE, TIP.NORMAL_CLOSE);
      LOGGER.d("manual close. result=" + isClosed);
      if (null != this.wsStatusListener) {
        if (isClosed) {
          this.wsStatusListener.onClosed(CODE.NORMAL_CLOSE, TIP.NORMAL_CLOSE);
        } else {
          this.wsStatusListener.onClosed(CODE.ABNORMAL_CLOSE, TIP.ABNORMAL_CLOSE);
        }
      }
      currentStatus = Status.DISCONNECTED;
    } else {
      LOGGER.w("state is illegal. status=" + currentStatus + ", websockdet=" + this.webSocket);
    }
  }

  static class CODE {
    public final static int NORMAL_CLOSE = 1000;
    public final static int ABNORMAL_CLOSE = 1001;
  }

  static class TIP {
    public final static String NORMAL_CLOSE = "normal close";
    public final static String ABNORMAL_CLOSE = "abnormal close";
  }

  private void connected() {
    cancelReconnect();
  }

  private void cancelReconnect() {
    reconnectCount = 0;
    // TODO: fix me!
    try {
      reconnectTimer.cancel();
    } catch (Exception ex) {
      ;
    }
  }

  private boolean tryReconnect() {
    if (!isNeedReconnect || isManualClose) {
      return false;
    }
    currentStatus = Status.RECONNECT;
    long delay = reconnectCount * RECONNECT_INTERVAL;
    if (delay > RECONNECT_MAX_TIME) {
      delay = RECONNECT_MAX_TIME;
    }
    reconnectCount++;
    reconnectTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (null != wsStatusListener) {
          wsStatusListener.onReconnect();
        }
        buildConnect();
      }
    }, delay);
    return true;
  }

  private synchronized void buildConnect() {
    if (Status.CONNECTED == this.currentStatus || Status.CONNECTING == this.currentStatus) {
      return;
    }
    this.currentStatus = Status.CONNECTING;
    initWebSocket();
  }

  private void initWebSocket() {
    try {
      lock.lockInterruptibly();
      try {
        client.dispatcher().cancelAll();
        client.newWebSocket(request, internalSocketListener);
      } finally {
        lock.unlock();
      }
    } catch (InterruptedException ex) {
      LOGGER.w("failed to initWebSocket", ex);
    }
  }
}
