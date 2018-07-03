package cn.leancloud.im;

public class InternalConfiguration {
  private static EventBroadcast eventBroadcast = null;
  private static CommandCarrier commandCarrier = null;

  public static EventBroadcast getEventBroadcast() {
    return eventBroadcast;
  }

  public static void setEventBroadcast(EventBroadcast eventBroadcast) {
    InternalConfiguration.eventBroadcast = eventBroadcast;
  }

  public static CommandCarrier getCommandCarrier() {
    return commandCarrier;
  }

  public static void setCommandCarrier(CommandCarrier commandCarrier) {
    InternalConfiguration.commandCarrier = commandCarrier;
  }
}
