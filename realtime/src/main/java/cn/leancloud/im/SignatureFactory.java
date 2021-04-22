package cn.leancloud.im;

import cn.leancloud.LCException;

import java.util.List;

public interface SignatureFactory {
  int SIGNATURE_FAILED_LOGIN = 4102;

  /**
   * 实现一个基础签名方法 其中的签名算法会在SessionManager和AVIMClient(V2)中被使用
   *
   * @param peerId peer id
   * @param watchIds watch peer ids.
   * @return signature object
   * @throws SignatureException 如果签名计算中间发生任何问题请抛出本异常
   */
  Signature createSignature(String peerId, List<String> watchIds) throws SignatureException;

  /**
   * 实现AVIMConversation相关的签名计算
   *
   * @param conversationId conversation id
   * @param clientId  owner client id
   * @param targetIds 操作所对应的数据
   * @param action - 此次行为的动作，行为分别对应常量 invite（加群和邀请）和 kick（踢出群）
   * @return signature object
   * @throws SignatureException 如果签名计算中间发生任何问题请抛出本异常
   */
  Signature createConversationSignature(String conversationId, String clientId,
                                               List<String> targetIds, String action) throws SignatureException;

  /**
   * 实现黑名单相关的签名计算
   *
   * @param clientId        当前登录用户的 id，必须
   * @param conversationId  目标对话的 id，可选
   * @param memberIds       目标成员的 id，可选
   * @param action          操作类型的字符串，有如下集中类型：
   *                        conversation-block-clients    在对话中拉黑部分成员，此时 conversationId 和 memberIds 是必须的。
   *                        conversation-unblock-clients  在对话中解禁部分成员，此时 conversationId 和 memberIds 是必须的。
   * @return signature object
   * @throws SignatureException 如果签名计算中间发生任何问题请抛出本异常
   */
  Signature createBlacklistSignature(String clientId, String conversationId, List<String> memberIds,
                                            String action) throws SignatureException;

  /**
   * 签名异常类
   */
  class SignatureException extends LCException {
    public SignatureException(int theCode, String theMessage) {
      super(theCode, theMessage);
    }
  }
}
