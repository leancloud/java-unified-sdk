package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.Messages;
import cn.leancloud.command.CommandPacket;
import cn.leancloud.command.PushAckPacket;
import cn.leancloud.utils.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.List;

public class WindTalker {
  private static final AVLogger LOGGER = LogUtil.getLogger(WindTalker.class);
  private static WindTalker instance = null;
  public static WindTalker getInstance() {
    if (null == instance) {
      instance = new WindTalker();
    }
    return instance;
  }
  private WindTalker() {
    ;
  }

  public CommandPacket assembleSessionOpenPacket() {
    return null;
  }

  public CommandPacket assemblePushAckPacket(String installationId, List<String> messageIds) {
    PushAckPacket pushAckPacket = new PushAckPacket();
    pushAckPacket.setInstallationId(installationId);
    pushAckPacket.setMessageIds(messageIds);
    return pushAckPacket;
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
