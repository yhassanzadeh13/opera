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
  public static String VictoryMessage = "Victory";
  public static String ElectionMessage = "Election";

  String payload;
  UUID senderId;
  UUID targetId;

  @Override
  public String logMessage() {
    return payload;
  }

  @Override
  public int size() {
    return 0;
  }

  /**
   * Message that transferred between nodes.
   *
   * @param msg message that sent to target node.
   * @param originalId sender of the message.
   * @param targetId receiver of the message.
   */
  public Message(String msg, UUID originalId, UUID targetId) {
    this.payload = msg;
    this.senderId = originalId;
    this.targetId = targetId;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public void setSenderId(UUID senderId) {
    this.senderId = senderId;
  }

  public void setTargetId(UUID targetId) {
    this.targetId = targetId;
  }

  public String getPayload() {
    return payload;
  }

  public UUID getSenderId() {
    return senderId;
  }

  public UUID getTargetId() {
    return targetId;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    MyNode node = (MyNode) hostNode;
    if (this.payload.equals(VictoryMessage)) {
      System.out.println("içerideyix");
      node.setCoordinatorId(this.senderId);
    } else if (this.payload.equals(ElectionMessage)) {
      node.sendMessage();
    }
    return true;
  }


}
