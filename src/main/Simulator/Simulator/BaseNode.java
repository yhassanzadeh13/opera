package Simulator;


import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;


/*
This the the base interface for the nodes in the simulator.

onStart: to initial the node. It receive the UUID for all the nodes in the cluster.
onStop: is called whenever the node wants to terminate. It is used for collecting garbage
        and possibly obtain certain log.
onNewMessage: is used for communication between the node itself and other nodes

 */
public interface BaseNode{


    void onStart(ArrayList<UUID> allID);
    void onStop();
    void onNewMessage(UUID originID, Event msg);
    BaseNode newInstance(UUID selfID);
}
