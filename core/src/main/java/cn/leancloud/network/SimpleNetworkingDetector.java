package cn.leancloud.network;

import jdk.nashorn.internal.runtime.regexp.RegExp;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class SimpleNetworkingDetector implements NetworkingDetector {
  private static final String eth0Pattern = "[1-9]+\\.[1-9]+\\.[1-9]+\\.[1-9]+";

  public boolean isConnected() {
    boolean result = false;
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();
        boolean isEth0 = false;
        for (InterfaceAddress i : networkInterface.getInterfaceAddresses()) {
          if ("127.0.0.1".equalsIgnoreCase(i.getAddress().getHostAddress())) {
            break;
          }
          if (Pattern.matches(eth0Pattern, i.getAddress().getHostAddress())) {
            isEth0 = true;
            break;
          }
        } // end for
        if (isEth0) {
          result = networkInterface.isUp();
          break;
        }
      } // end while

      return result;
    } catch (SocketException ex)  {
      return false;
    }
  }

  public NetworkType getNetworkType() {
    return NetworkType.WIFI;
  }
}
