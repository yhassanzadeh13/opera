package Simulator;

import java.io.Serializable;

/**
 * The Event interface is a base interface for the events that the nodes will
 * communicate with each through.
 */

public interface Event extends Serializable {

    /**
     * Should be activated by the node in order for the event to take action
     */
    void actionPerformed();

    /**
     * This method is used for log purpose. It will be activated by the simulator to log the
     * event during simulation.
     * @return a string that represents the log message of the event.
     */
    String logMessage();
}
