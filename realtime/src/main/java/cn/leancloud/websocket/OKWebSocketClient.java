package cn.leancloud.websocket;

import cn.leancloud.LCLogger;
import cn.leancloud.Messages;
import cn.leancloud.utils.LogUtil;
import okhttp3.*;
import okio.ByteString;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
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
public class OKWebSocketClient {
  private static LCLogger gLogger = LogUtil.getLogger(OKWebSocketClient.class);
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
      gLogger.d("onOpen");
      OKWebSocketClient.this.webSocket = webSocket;
      OKWebSocketClient.this.currentStatus = Status.CONNECTED;
      connected();
      if (null != wsStatusListener) {
        wsStatusListener.onOpen(response);
      }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
      gLogger.d("onMessage(text): " + text);
      if (null != wsStatusListener) {
        wsStatusListener.onMessage(text);
      }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
      try {
        Messages.GenericCommand command = Messages.GenericCommand.parseFrom(bytes.toByteArray());
        gLogger.d("downLink: " + command.toString());
      } catch (Exception ex) {
        gLogger.d("onMessage " + bytes.utf8());
      }
      if (null != wsStatusListener) {
        wsStatusListener.onMessage(bytes);
      }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
      gLogger.d("onClosing");
      if (null != wsStatusListener) {
        wsStatusListener.onClosing(code, reason);
      }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
      gLogger.d("onClosed");
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
      gLogger.w("onFailure", t);
      if (null != wsStatusListener) {
        wsStatusListener.onFailure(t, response);
      }
    }
  };

  public enum Status {
    DISCONNECTED, CONNECTED, CONNECTING, CLOSING, RECONNECT;
  }

  public OKWebSocketClient(WsStatusListener externalListener, boolean needReconnect) {
    this.wsStatusListener = externalListener;
    this.isNeedReconnect = needReconnect;
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .pingInterval(120, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS);
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length >= 1 && (trustManagers[0] instanceof X509TrustManager)) {
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { trustManager }, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        builder.sslSocketFactory(sslSocketFactory, trustManager);
      }
    } catch (Exception ex) {
      gLogger.w(ex);
    }
    this.client = builder.retryOnConnectionFailure(true)
            .addInterceptor(new Interceptor() {
              public Response intercept(Interceptor.Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request newRequest = originalRequest.newBuilder()
                        .header("Sec-WebSocket-Protocol", "lc.protobuf2.3").build();
                return chain.proceed(newRequest);
              }
            })
            .build();
  }

  public Status getCurrentStatus() {
    return this.currentStatus;
  }

  public boolean sendMessage(String msg) {
    return this.webSocket.send(msg);
  }

  public boolean sendMessage(ByteString byteString) {
    return this.webSocket.send(byteString);
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
      gLogger.d("manual close. result=" + isClosed);
      if (null != this.wsStatusListener) {
        if (isClosed) {
          this.wsStatusListener.onClosed(CODE.NORMAL_CLOSE, TIP.NORMAL_CLOSE);
        } else {
          this.wsStatusListener.onClosed(CODE.ABNORMAL_CLOSE, TIP.ABNORMAL_CLOSE);
        }
      }
      currentStatus = Status.DISCONNECTED;
    } else {
      gLogger.w("state is illegal. status=" + currentStatus + ", websockdet=" + this.webSocket);
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
      gLogger.w(ex);
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
      gLogger.w("failed to initWebSocket", ex);
    }
  }
}
