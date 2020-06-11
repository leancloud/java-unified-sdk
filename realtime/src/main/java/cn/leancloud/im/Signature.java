package cn.leancloud.im;

import java.util.Collections;
import java.util.List;

//@JSONType(ignores = {"expired"})
public class Signature {

  private String signature;

  private long timestamp;

  private String nonce;

  private List<String> signedPeerIds;

  @Deprecated
  public List<String> getSignedPeerIds() {
    if (signedPeerIds == null) {
      signedPeerIds = Collections.emptyList();
    }
    return signedPeerIds;
  }

  @Deprecated
  public void setSignedPeerIds(List<String> signedPeerIds) {
    this.signedPeerIds = signedPeerIds;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  protected boolean isExpired() {
    return timestamp + 14400 < (System.currentTimeMillis() / 1000);
  }

}
