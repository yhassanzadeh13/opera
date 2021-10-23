package examples.bullyalgorithm;

import java.io.Serializable;
import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;

/**
 * Message class contains a message to transfer between nodes. In this examples messages can be either VictoryMessage or
 * ElectionMessage. VictoryMessage declares a victory and  nodes send ElectionMessage to other nodes with bigger ID's to
 * find the coordinator.
 */
public class Message implements Event, Serializable {
  public static String VictoryMessage = "victory";
  public static String ElectionMessage = "election";

  String msg;
  UUID originalId;
  UUID targetId;

  @Override
  public String logMessage() {
    return msg;
  }

  @Override
  public int size() {
    return 0;
  }

  /**
   * Message that transferred between nodes.
   *
   * @param msg message that sent to target node.
   * @param originalId Sender of the message.
   * @param targetId Reciever of the message.
   */
  public Message(String msg, UUID originalId, UUID targetId) {
    this.msg = msg;
    this.originalId = originalId;
    this.targetId = targetId;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public void setOriginalId(UUID originalId) {
    this.originalId = originalId;
  }

  public void setTargetId(UUID targetId) {
    this.targetId = targetId;
  }

  public String getMsg() {
    return msg;
  }

  public UUID getOriginalId() {
    return originalId;
  }

  public UUID getTargetId() {
    return targetId;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    MyNode node = (MyNode) hostNode;
    node.coordinatorId = node.getMaxId();
    if (this.getMsg().equals(VictoryMessage)) {
      node.setCoordinatorId(this.originalId);

    } else if (this.getMsg().equals(ElectionMessage)) {
      node.sendMessage();
    }

    return true;
  }


}
