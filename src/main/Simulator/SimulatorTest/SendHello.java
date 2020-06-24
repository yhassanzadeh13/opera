package SimulatorTest;

import Simulator.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.UUID;

public class SendHello implements Event, Serializable {

    @JsonDeserialize(as = Event.class)

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
    public void actionPerformed() {
        System.out.println(origionalID + " says " + msg);
        if(msg.equals("Hello"))Simulator.Submit(targetID, origionalID, new SendHello("Thank you", targetID, origionalID));
    }

}
