package cn.leancloud.livequery;

import java.util.ArrayList;

public class AVLiveQuery {
  public enum EventType {
    CREATE("create"), UPDATE("update"), ENTER("enter"), LEAVE("leave"), DELETE("delete"), LOGIN("login"), UNKONWN("unknown");

    private String event;

    public static EventType getType(String event) {
      if (CREATE.getContent().equals(event)) {
        return CREATE;
      } else if (UPDATE.getContent().equals(event)) {
        return UPDATE;
      } else if (ENTER.getContent().equals(event)) {
        return ENTER;
      } else if (LEAVE.getContent().equals(event)) {
        return LEAVE;
      } else if (DELETE.getContent().equals(event)) {
        return DELETE;
      } else if (LOGIN.getContent().equals(event)) {
        return LOGIN;
      }
      return UNKONWN;
    }

    EventType(String event) {
      this.event = event;
    }

    public String getContent() {
      return event;
    }
  }

  public static void processData(ArrayList<String> dataList) {
    ;
  }
}
