package cn.leancloud.im;

import java.util.List;

public class AVUserSignatureFactory implements SignatureFactory {
  private String sessionToken;
  public AVUserSignatureFactory(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  public Signature createSignature(String peerId, List<String> watchIds) throws SignatureException {
    return null;
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
