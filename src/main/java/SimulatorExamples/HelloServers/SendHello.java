package SimulatorExamples.HelloServers;

import Node.BaseNode;
import Underlay.packets.Event;

import java.io.Serializable;
import java.util.UUID;

public class SendHello implements Event, Serializable {
    String msg;
    UUID originalID;
    UUID targetID;

    public SendHello(String msg, UUID originalID, UUID targetID) {
        this.msg = msg;
        this.originalID = originalID;
        this.targetID = targetID;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UUID getOriginalID() {
        return originalID;
    }

    public void setOriginalID(UUID originalID) {
        this.originalID = originalID;
    }

    public UUID getTargetID() {
        return targetID;
    }

    public void setTargetID(UUID targetID) {
        this.targetID = targetID;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        System.out.println(originalID + " says to " + targetID + " " + msg);
        myNode node = (myNode) hostNode;
        if (this.msg.equals("Hello"))
            node.sendNewMessage("Thank You");
        else
            node.sendNewMessage("Hello");
        return true;
    }

    @Override
    public String logMessage() {
        return msg;
    }

    @Override
    public int size(){
        return msg.length();
    }

}
