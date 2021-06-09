package cn.leancloud.im;

import cn.leancloud.service.RealtimeClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LCUserSignatureFactory implements SignatureFactory {
  private String sessionToken;
  public LCUserSignatureFactory(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public Signature createSignature(String peerId, List<String> watchIds) throws SignatureException {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("session_token", sessionToken);
    return RealtimeClient.getInstance().createSignature(data).blockingFirst();
  }

  public Signature createConversationSignature(String conversationId, String clientId,
                                        List<String> targetIds, String action) throws SignatureException {
    return null;
  }

  public Signature createBlacklistSignature(String clientId, String conversationId, List<String> memberIds,
                                     String action) throws SignatureException {
    return null;
  }
}
