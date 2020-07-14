package SimulatorExamples.HelloServers;

import Simulator.*;

import java.io.Serializable;
import java.util.UUID;

public class SendHello implements Event, Serializable {


    String msg;
    UUID origionalID;
    UUID targetID;

    @Override
    public String logMessage() {
        return msg;
    }

    public SendHello(String msg, UUID origionalID, UUID targetID) {
        this.msg = msg;
        this.origionalID = origionalID;
        this.targetID = targetID;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setOrigionalID(UUID origionalID) {
        this.origionalID = origionalID;
    }

    public void setTargetID(UUID targetID) {
        this.targetID = targetID;
    }

    public String getMsg() {
        return msg;
    }

    public UUID getOrigionalID() {
        return origionalID;
    }

    public UUID getTargetID() {
        return targetID;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        System.out.println(origionalID + " says to " + targetID + " " + msg);
        myNode node = (myNode) hostNode;
        if(this.msg.equals("Hello"))
            node.sendNewMessage("Thank You");
        else
            node.sendNewMessage("Hello");
        return true;
    }

}
