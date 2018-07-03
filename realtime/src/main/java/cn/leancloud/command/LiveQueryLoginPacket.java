package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.utils.StringUtil;

public class LiveQueryLoginPacket extends LoginPacket {
  public static final int SERVICE_LIVE_QUERY = 1;
  public static final int SERVICE_PUSH = 0;
  private String subscribeId;

  public void setSubscribeId(String subscribeId) {
    this.subscribeId = subscribeId;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    if (!StringUtil.isEmpty(subscribeId)) {
      builder.setInstallationId(subscribeId);
      builder.setService(SERVICE_LIVE_QUERY);
    }
    return builder;
  }
}
