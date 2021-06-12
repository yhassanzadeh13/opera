package SimulatorExamples.BullyAlgorithm;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.packets.Event;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Event, Serializable {

    String msg;
    UUID originalID;
    UUID targetID;

    @Override
    public String logMessage() {
        return msg;
    }

    public Message(String msg, UUID originalID, UUID targetID) {
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
        System.out.println(originalID + " sends to" + targetID + " " + msg);
        MyNode node = (MyNode) hostNode;
        if(this.msg.equals("Victory"))
            node.setCoordinatorID(this.originalID);
        else if (this.msg.equals("Election")){
            node.sendNewMessagetoBigger();
        }

        return true;
    }


}
