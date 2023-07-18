package network.model;

import java.io.Serializable;
import java.sql.Timestamp;

import node.Identifier;

/**
 * Message is the unit of communications between the nodes at the networking layer of Opera.
 * It wraps an encoded application layer event with the sender and receiver identifiers. A Message is exchanged
 * between the networking layers of two nodes.
 */
public class Message implements Serializable {
  /**
   * The timestamp of when the message was sent.
   */
  private final Timestamp sentTimeStamp;

  /**
   * The encoded event.
   */
  private final byte[] event;

  /**
   * The identifier of the sender of the message.
   */
  private final Identifier originId;

  /**
   * The identifier of the receiver (target) of the message.
   */
  private final Identifier targetId;

  /**
   * Constructor for message.
   *
   * @param event    the encoded event (use Encoder to encode the event into bytes).
   * @param origin   identifier of the node sending the event.
   * @param targetId identifier of the node receiving the event.
   */
  public Message(byte[] event, Identifier origin, Identifier targetId) {
    this.event = event;
    this.originId = origin;
    this.targetId = targetId;
    this.sentTimeStamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * Returns the encoded event in bytes.
   *
   * @return the encoded event in bytes.
   */
  public byte[] getEncodedEvent() {
    return event;
  }

  /**
   * Returns the identifier of the sender of the message.
   *
   * @return identifier of the sender of the message.
   */
  public Identifier getOriginId() {
    return originId;
  }

  /**
   * Returns the identifier of the receiver (target) of the message.
   *
   * @return identifier of the receiver (target) of the message.
   */
  public Identifier getTargetId() {
    return targetId;
  }

  /**
   * Returns the timestamp of when the message was sent.
   *
   * @return the timestamp of when the message was sent.
   */
  public Timestamp getSentTimeStamp() {
    return new Timestamp(this.sentTimeStamp.getTime());
  }
}
