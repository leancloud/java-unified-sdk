package cn.leancloud.command;

import cn.leancloud.Messages;

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
