package Simulator;

import java.util.UUID;

public class Message {


    UUID originalID;

    public UUID getOriginalID() {
        return originalID;
    }

    public UUID getTargetID() {
        return targetID;
    }

    public byte[] getMessage() {
        return payload;
    }

    UUID targetID;
    byte[] payload;

    public Message(UUID originalID, UUID targetID, byte[] payload) {
        this.originalID = originalID;
        this.targetID = targetID;
        this.payload = payload;
    }



}
