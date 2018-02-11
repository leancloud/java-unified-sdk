package cn.leancloud.network;

import cn.leancloud.utils.StringUtil;
import okhttp3.Dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class DNSDetoxicant implements Dns {
  public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    if (StringUtil.isEmpty(hostname)) {
      throw new UnknownHostException("hostname is empty");
    }
    try {
      return Arrays.asList(InetAddress.getAllByName(hostname));
    } catch (UnknownHostException ex) {
      // TODO: parse host name through DNSPod.
      throw ex;
    }
  }
}
