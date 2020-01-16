package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.Conversation;
import cn.leancloud.utils.StringUtil;

import java.util.Arrays;
import java.util.Map;

public class ConversationQueryPacket extends PeerBasedCommandPacket {
  Map<String, Object> queryParams;

  public ConversationQueryPacket() {
    this.setCmd("conv");
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setConvMessage(getConvCommand());
    builder.setOp(Messages.OpType.valueOf(ConversationControlPacket.ConversationControlOp.QUERY));
    return builder;
  }

  protected Messages.ConvCommand getConvCommand() {
    Messages.ConvCommand.Builder builder = Messages.ConvCommand.newBuilder();
    if (null != queryParams && !queryParams.isEmpty()) {
      // add for support temporary conversation query.
      Object tempConvId = queryParams.get(Conversation.QUERY_PARAM_TEMPCONV);
      if (null != tempConvId && !StringUtil.isEmpty(tempConvId.toString())) {
        builder.addAllTempConvIds(Arrays.asList(tempConvId.toString().split(",")));
      }

      Object sortParam = queryParams.get("order");
      if (null != sortParam && !StringUtil.isEmpty(sortParam.toString())) {
        builder.setSort(sortParam.toString());
      }

      Object skipParam = queryParams.get(Conversation.QUERY_PARAM_OFFSET);
      if (null != skipParam && !StringUtil.isEmpty(skipParam.toString())) {
        builder.setSkip(Integer.parseInt(skipParam.toString()));
      }

      Object limitParam = queryParams.get(Conversation.QUERY_PARAM_LIMIT);
      if (null != limitParam && !StringUtil.isEmpty(limitParam.toString())) {
        builder.setLimit(Integer.parseInt(limitParam.toString()));
      }

      Object whereParam = queryParams.get(Conversation.QUERY_PARAM_WHERE);
      if (null != whereParam && !StringUtil.isEmpty(whereParam.toString())) {
        Messages.JsonObjectMessage.Builder messageBuild = Messages.JsonObjectMessage.newBuilder();
        messageBuild.setData(whereParam.toString());
        builder.setWhere(messageBuild);
      }

      int flag = 0;
      Object lastMessage = queryParams.get(Conversation.QUERY_PARAM_LAST_MESSAGE);
      if (null != lastMessage && !StringUtil.isEmpty(lastMessage.toString())) {
        if (Boolean.parseBoolean(lastMessage.toString())) {
          flag |= 1 << 1;
        }
      }

      Object compact = queryParams.get(Conversation.QUERY_PARAM_COMPACT);
      if (null != compact && !StringUtil.isEmpty(compact.toString())) {
        if (Boolean.parseBoolean(compact.toString())) {
          flag |= 1;
        }
      }

      if (flag > 0) {
        builder.setFlag(flag);
      }
    }
    return builder.build();
  }

  public static ConversationQueryPacket getConversationQueryPacket(String peerId, Map<String, Object> queryParams, int requestId) {
    ConversationQueryPacket cqp = new ConversationQueryPacket();
    if (AVIMClient.getClientsCount() > 1) {
      // peerId is necessary only when more than 1 client logined.
      cqp.setPeerId(peerId);
    }
    cqp.queryParams = queryParams;
    cqp.setRequestId(requestId);
    return cqp;
  }
}
