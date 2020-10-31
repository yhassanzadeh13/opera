package Underlay.packets;

import java.util.UUID;

/**
 * This method is for the package Similator level and is not supposed to be accessed by the nodes.
 * It serves as the base message between the LocalUnderlay and the Simulator.Simulator. The Simulator.Simulator will marshal
 * the Event and capsule it into Message before sending it to the LocalUnderlay layer.
 */

class Message {


    private UUID originalID;
    private UUID targetID;
    private byte[] payload;

    /**
     *
     * @param originalID the ID of the sender node
     * @param targetID the ID of the receiver node
     * @param payload the marshaled version of the Event
     */

    public Message(UUID originalID, UUID targetID, byte[] payload) {
        this.originalID = originalID;
        this.targetID = targetID;
        this.payload = payload;
    }

    /**
     * Getter for the ID of the sender node
     * @return ID of the sender node
     */
    public UUID getOriginalID() {
        return originalID;
    }

    /**
     * Getter for the ID of the sender node
     * @return ID of the sender node
     */
    public UUID getTargetID() {
        return targetID;
    }

    /**
     * Getter for the marshaled (byte array) Event.
     * @return byte array that represents the marshaled Event of the message
     */
    public byte[] getMessage() {
        return payload;
    }






}
