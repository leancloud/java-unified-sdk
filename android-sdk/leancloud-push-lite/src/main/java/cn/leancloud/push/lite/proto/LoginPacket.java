package cn.leancloud.push.lite.proto;

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
