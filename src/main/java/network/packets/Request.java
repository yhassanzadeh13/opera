package network.packets;

import java.io.Serializable;
import java.sql.Timestamp;

import node.Identifier;

/**
 * Represents a serializable request packet what wrap an event.
 */
public class Request implements Serializable {
  private final Timestamp sentTimeStamp;
  private final Event event;
  private final Identifier originalId;
  private final Identifier destinationId;

  /**
   * Constructor for the Request.
   *
   * @param event         Event of the request
   * @param originalId    sender of the request
   * @param destinationId reciever of the request
   */
  public Request(Event event, Identifier originalId, Identifier destinationId) {
    this.event = event;
    this.originalId = originalId;
    this.destinationId = destinationId;
    this.sentTimeStamp = new Timestamp(System.currentTimeMillis());
  }

  public Event getEvent() {
    return event;
  }

  public Identifier getOriginalId() {
    return originalId;
  }

  public Identifier getDestinationId() {
    return destinationId;
  }

  public Timestamp getSentTimeStamp() {
    return new Timestamp(this.sentTimeStamp.getTime());
  }
}
