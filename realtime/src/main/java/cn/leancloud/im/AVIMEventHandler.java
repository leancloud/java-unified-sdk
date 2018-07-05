package cn.leancloud.im;

public abstract class AVIMEventHandler {
  public void processEvent(final int operation, final Object operator, final Object operand,
                           final Object eventScene) {
    processEvent0(operation, operator, operand, eventScene);
  };

  protected abstract void processEvent0(int operation, Object operator, Object operand,
                                        Object eventScene);
}