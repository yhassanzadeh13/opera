package simulatorexamples.helloservers;

import java.io.Serializable;
import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;


public class SendHello implements Event, Serializable {
  String msg;
  UUID originalId;
  UUID targetId;

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
