package SimulatorExamples.HelloServers;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import underlay.packets.Event;

import java.io.Serializable;
import java.util.UUID;

public class SendHello implements Event, Serializable {


    String msg;
    UUID originalID;
    UUID targetID;

    @Override
    public String logMessage() {
        return msg;
    }

    public SendHello(String msg, UUID originalID, UUID targetID) {
        this.msg = msg;
        this.originalID = originalID;
        this.targetID = targetID;
        SimulatorHistogram.observe("packetSize", originalID, msg.length());
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setOriginalID(UUID originalID) {
        this.originalID = originalID;
    }

    public void setTargetID(UUID targetID) {
        this.targetID = targetID;
    }

    public String getMsg() {
        return msg;
    }

    public UUID getOriginalID() {
        return originalID;
    }

    public UUID getTargetID() {
        return targetID;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        System.out.println(originalID + " says to " + targetID + " " + msg);
        myNode node = (myNode) hostNode;
        if(this.msg.equals("Hello"))
            node.sendNewMessage("Thank You");
        else
            node.sendNewMessage("Hello");
        return true;
    }

}
