package examples.helloservers;

import java.io.Serializable;
import java.util.UUID;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;

/**
 * HelloEvent is an event which enables node to send "Thank You" if the message is "Hello"
 * else sends "Hello" message back to that node.
 */
public class HelloEvent implements Event, Serializable {
  String msg;
  Identifier originalId;
  Identifier targetId;

  /**
   * Constructor of a Sendhello object.
   *
   * @param msg        message to send.
   * @param originalId Id of the sendeer node.
   * @param targetId   Id of the reciever node.
   */
  public HelloEvent(String msg, Identifier originalId, Identifier targetId) {
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

  public Identifier getOriginalId() {
    return originalId;
  }

  public void setOriginalId(Identifier originalId) {
    this.originalId = originalId;
  }

  public Identifier getTargetId() {
    return targetId;
  }

  public void setTargetId(Identifier targetId) {
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
