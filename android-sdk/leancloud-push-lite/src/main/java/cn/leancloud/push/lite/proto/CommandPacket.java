package cn.leancloud.push.lite.proto;

import cn.leancloud.push.lite.utils.StringUtil;

public class CommandPacket {
  private String cmd;

  /**
   * 只有在 login 时才需要 appId，其他时候不需要
   */
  private String appId;
  private int requestId = UNSUPPORTED_OPERATION;
  private String installationId;
  public static final int UNSUPPORTED_OPERATION = -65537;

  public int getRequestId() {
    return requestId;
  }

  public void setRequestId(int id) {
    this.requestId = id;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getInstallationId() {
    return installationId;
  }

  public void setInstallationId(String installationId) {
    this.installationId = installationId;
  }

  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = Messages.GenericCommand.newBuilder();
    if (!StringUtil.isEmpty(appId)) {
      builder.setAppId(appId);
    }

    builder.setCmd(Messages.CommandType.valueOf(getCmd()));
    if (getInstallationId() != null) {
      builder.setInstallationId(getInstallationId());
    }
    if (requestId > CommandPacket.UNSUPPORTED_OPERATION) {
      builder.setI(requestId);
    }
    return builder;
  }

  public Messages.GenericCommand getGenericCommand() {
    Messages.GenericCommand.Builder builder = getGenericCommandBuilder();
    return builder.build();
  }

  public int getLength() {
    return getGenericCommandBuilder().build().getSerializedSize();
  }
}
