package cn.leancloud.im;

public class InternalConfiguration {
  private static EventBoardcast eventBoardcast = null;
  private static CommandCarrier commandCarrier = null;

  public static EventBoardcast getEventBoardcast() {
    return eventBoardcast;
  }

  public static void setEventBoardcast(EventBoardcast eventBoardcast) {
    InternalConfiguration.eventBoardcast = eventBoardcast;
  }

  public static CommandCarrier getCommandCarrier() {
    return commandCarrier;
  }

  public static void setCommandCarrier(CommandCarrier commandCarrier) {
    InternalConfiguration.commandCarrier = commandCarrier;
  }
}
