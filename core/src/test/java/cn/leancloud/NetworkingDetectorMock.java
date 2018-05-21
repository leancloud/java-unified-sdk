package cn.leancloud;

import cn.leancloud.network.NetworkingDetector;

public class NetworkingDetectorMock implements NetworkingDetector {
  private volatile boolean isConnected = true;

  public void setConnected(boolean connected) {
    this.isConnected = connected;
  }

  public boolean isConnected() {
    return this.isConnected;
  }

  public NetworkType getNetworkType() {
    return NetworkType.WIFI;
  }
}
