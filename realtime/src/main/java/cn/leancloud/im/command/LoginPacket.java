package cn.leancloud.im.command;

import cn.leancloud.im.Messages;

public class LoginPacket extends CommandPacket {
  public LoginPacket() {
    this.setCmd("login");
  }

  @Override
  protected Messages.GenericCommand.Builder getGenericCommandBuilder() {
    Messages.GenericCommand.Builder builder = super.getGenericCommandBuilder();
    builder.setCmd(Messages.CommandType.login);
    return builder;
  }
}
