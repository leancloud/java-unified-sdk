package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.utils.StringUtil;

public class LiveQueryLoginPacket extends LoginPacket {
  public static final int SERVICE_LIVE_QUERY = 1;
  public static final int SERVICE_PUSH = 0;
  private String subscribeId;
  private long clientTs = 0;

  public void setSubscribeId(String subscribeId) {
    this.subscribeId = subscribeId;
  }

  public void setClientTs(long now) {
    this.clientTs = now;
  }
  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    if (!StringUtil.isEmpty(subscribeId)) {
      builder.setInstallationId(subscribeId);
      builder.setClientTs(clientTs);
      builder.setService(SERVICE_LIVE_QUERY);
    }
    return builder;
  }
}
