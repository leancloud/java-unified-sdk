package cn.leancloud.command;

import cn.leancloud.Messages;
import cn.leancloud.im.SystemReporter.*;

public class LoginPacket extends CommandPacket {
  public LoginPacket() {
    this.setCmd("login");
  }

  private SystemInfo systemInfo = null;

  public void setSystemInfo(SystemInfo systemInfo) {
    this.systemInfo = systemInfo;
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setCmd(Messages.CommandType.login);
    if (null != this.systemInfo) {
      Messages.LoginCommand.Builder loginBuilder = Messages.LoginCommand.newBuilder();
      Messages.SystemInfo.Builder systemInfoBuilder = Messages.SystemInfo.newBuilder();
      systemInfoBuilder.setIsEmulator(this.systemInfo.isRunOnEmulator()).setDeviceType(Messages.DeviceType.android);
      systemInfoBuilder.setAndroidVersion(
              Messages.AndroidVersion.newBuilder().setApiLevel(String.valueOf(this.systemInfo.getOsAPILevel()))
                      .setCodename(this.systemInfo.getOsCodeName()));
      String buildAttr = this.systemInfo.getManufacturer()+ "/" + this.systemInfo.getBrand() + "/"
              + this.systemInfo.getModel();
      systemInfoBuilder.setOsVersion(Messages.SemanticVersion.newBuilder().setBuild(buildAttr).build());
      loginBuilder.setSystemInfo(systemInfoBuilder.build());
      builder.setLoginMessage(loginBuilder);
    }

    return builder;
  }
}
