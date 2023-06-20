package network.packets;

import java.io.Serializable;


/**
 * Event is a marker interface for all events, it abstracts the unit of communications between the nodes at the application layer,
 * i.e., layer 7 of the OSI model. Currently, we don't necessitate any methods to be implemented by the events. It being
 * a marker interface is sufficient for the time being.
 */
public interface Event extends Serializable {

}
