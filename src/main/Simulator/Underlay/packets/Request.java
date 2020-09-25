package Underlay.packets;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a serializable request packet what wrap an event.
 */
public class Request implements Serializable {

    private final Event event;
    private final UUID orginalID;
    private final UUID destinationID;

    public Request(Event event, UUID orginalID, UUID destinationID) {
        this.event = event;
        this.orginalID = orginalID;
        this.destinationID = destinationID;
    }

    public Event getEvent() {
        return event;
    }

    public UUID getOrginalID() {
        return orginalID;
    }

    public UUID getDestinationID() {
        return destinationID;
    }
}
