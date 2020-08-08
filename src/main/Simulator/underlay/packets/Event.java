package underlay.packets;

import Node.BaseNode;

import java.io.Serializable;

/**
 * The Event interface is a base interface for the events that the nodes will
 * communicate with each other through.
 */

public interface Event extends Serializable{

    /**
     * Should be activated by the event host node in order for the event to take action
     * This method should be called in the event host node by calling event.actionPerformed(this)
     * @param hostNode an instance of the host node in order to perform the event action. Please note that
     *             this parameter is of the type BaseNode. Therefore, in order to access the node params
     *             and methods, you should down cast the node into your special node class
     * @return True if action was performed successfully. False otherwise
     */
    boolean actionPerformed(BaseNode hostNode);

    /**
     * This method is used for log purpose. It will be activated by the simulator to log the
     * event during simulation.
     * @return a string that represents the log message of the event.
     */
    String logMessage();
}
