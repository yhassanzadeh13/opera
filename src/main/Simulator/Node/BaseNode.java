package Node;


import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.UUID;


/**
The BaseNode interface represents a base interface for the node class to be available for the Simulator.
onStart: to initial the node. It receive the UUID for all the nodes in the cluster.
onStop: is called whenever the node wants to terminate. It is used for collecting garbage
        and possibly obtain certain log.
onNewMessage: is used for communication between the node itself and other nodes

 */

public interface BaseNode{


    /**
     * Activated by the Simulator on the creation of a new node and is used to initialize the node parameters
     * @param allID the IDs of type UUID for all the nodes in the cluster
     */
    void onCreate(ArrayList<UUID> allID);

    /**
     * Activated by the Simulator after all the nodes in the cluster become ready
     */
    void onStart();

    /**
     * Activated by the Simulator just before the node shuts down.
     */
    void onStop();

    /**
     * A channel for a node to receive a new event from other nodes.
     * The node will receive all the message through this method.
     * @param originID the ID of the sender node
     * @param msg the content of the message
     */
    void onNewMessage(UUID originID, Event msg);

    /**
     * This method serves as a factory for new node instances.
     * It is supposed to return a new instance of the special node class
     * @param selfID the ID of the new node
     * @return a new instance of the special node class
     */
    BaseNode newInstance(UUID selfID, MiddleLayer network);

}
