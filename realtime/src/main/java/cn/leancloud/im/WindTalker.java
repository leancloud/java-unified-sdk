package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.LiveQueryLoginPacket;
import cn.leancloud.command.PushAckPacket;
import cn.leancloud.command.SessionControlPacket;
import cn.leancloud.utils.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WindTalker {
  private static final AVLogger LOGGER = LogUtil.getLogger(WindTalker.class);
  private static WindTalker instance = null;
  static AtomicInteger acu = new AtomicInteger(-65536);
  public static WindTalker getInstance() {
    if (null == instance) {
      instance = new WindTalker();
    }
    return instance;
  }
  private WindTalker() {
    ;
  }

  public static int getNextIMRequestId() {
    int val = acu.incrementAndGet();
    if (val > 65535) {
      while (val > 65535 && !acu.compareAndSet(val, -65536)) {
        val = acu.get();
      }
      return val;
    } else {
      return val;
    }
  }

  public CommandPacket assembleSessionOpenPacket(String clientId, String tag, Signature signature, long lastNotifyTime,
                                                 long lastPatchTime, boolean reConnect, int requestId) {
    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
            clientId, null,
            SessionControlPacket.SessionControlOp.OPEN, signature,
            lastNotifyTime, lastPatchTime, requestId);
    scp.setTag(tag);
    scp.setReconnectionRequest(reConnect);
    return scp;
  }

  public CommandPacket assembleSessionPacket(String selfId, List<String> peers,
                                             String op, Signature signature, Integer requestId) {
    SessionControlPacket scp = SessionControlPacket.genSessionCommand(selfId, peers, op, signature, requestId);
    return scp;
  }

  public CommandPacket assemblePushAckPacket(String installationId, List<String> messageIds) {
    PushAckPacket pushAckPacket = new PushAckPacket();
    pushAckPacket.setInstallationId(installationId);
    pushAckPacket.setMessageIds(messageIds);
    return pushAckPacket;
  }

  public CommandPacket assembleLiveQueryLoginPacket(String subscriptionId, int requestId) {
    LiveQueryLoginPacket lp = new LiveQueryLoginPacket();
    lp.setSubscribeId(subscriptionId);
    lp.setClientTs(System.currentTimeMillis());
    if (0 != requestId) {
      lp.setRequestId(requestId);
    }
    return lp;
  }
  public Messages.GenericCommand disassemblePacket(ByteBuffer bytes) {
    try {
      return Messages.GenericCommand.parseFrom(bytes);
    } catch (InvalidProtocolBufferException ex) {
      LOGGER.e("failed to disassemble packet.", ex);
      return null;
    }
  }
}
