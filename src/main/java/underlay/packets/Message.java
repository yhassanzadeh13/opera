package underlay.packets;

import java.util.UUID;

/**
 * This method is for the package Simulator level and is not supposed to be accessed by the nodes.
 * It serves as the base message between the LocalUnderlay and the simulator.simulator. The simulator.
 * Simulator will marshal the Event and capsule it into Message before sending it to the LocalUnderlay layer.
 */

class Message {


  private UUID originalId;
  private UUID targetId;
  private byte[] payload;

  /**
   * Constructor for the Message.
   *
   * @param originalId the ID of the sender node
   * @param targetId   the ID of the receiver node
   * @param payload    the marshaled version of the Event
   */

  public Message(UUID originalId, UUID targetId, byte[] payload) {
    this.originalId = originalId;
    this.targetId = targetId;
    this.payload = payload;
  }

  /**
   * Getter for the ID of the sender node.
   *
   * @return ID of the sender node
   */
  public UUID getOriginalId() {
    return originalId;
  }

  /**
   * Getter for the ID of the sender node.
   *
   * @return ID of the sender node
   */
  public UUID getTargetId() {
    return targetId;
  }

  /**
   * Getter for the marshaled (byte array) Event.
   *
   * @return byte array that represents the marshaled Event of the message
   */
  public byte[] getMessage() {
    return payload;
  }


}
