package examples.helloservers;

import java.io.Serializable;
import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;

/**
 * Sendhello is an event which enables node to send "Thank You" if the message is "Hello"
 * else sends "Hello" message back to that node.
 */
public class SendHello implements Event, Serializable {
  String msg;
  UUID originalId;
  UUID targetId;

  /**
   *  Constructor of a Sendhello object.
   *
   * @param msg message to send.
   * @param originalId Id of the sendeer node.
   * @param targetId Id of the reciever node.
   */
  public SendHello(String msg, UUID originalId, UUID targetId) {
    this.msg = msg;
    this.originalId = originalId;
    this.targetId = targetId;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public UUID getOriginalId() {
    return originalId;
  }

  public void setOriginalId(UUID originalId) {
    this.originalId = originalId;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public void setTargetId(UUID targetId) {
    this.targetId = targetId;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    System.out.println(originalId + " says to " + targetId + " " + msg);
    MyNode node = (MyNode) hostNode;
    if (this.msg.equals("Hello")) {
      node.sendNewMessage("Thank You");
    } else {
      node.sendNewMessage("Hello");
    }
    return true;
  }

  @Override
  public String logMessage() {
    return msg;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
