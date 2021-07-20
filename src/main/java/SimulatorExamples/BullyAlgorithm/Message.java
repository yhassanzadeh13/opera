package SimulatorExamples.BullyAlgorithm;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.packets.Event;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Event, Serializable {
    public static String VictoryMessage = "Victory";
    public static String ElectionMessage = "Election";

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
        MyNode node = (MyNode) hostNode;
        node.coordinatorID = node.getMaxID();
        if(this.getMsg().equals(VictoryMessage)) {
            node.setCoordinatorID(this.originalID);

        }
        else if (this.getMsg().equals(ElectionMessage)){
            node.SendMessage();
        }

        return true;
    }


}
