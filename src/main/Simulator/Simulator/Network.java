package Simulator;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


/**
 * Serves as the Network layer of the simulator
 */

public class Network {

    final private ArrayList<UUID> allID;
    private HashMap<UUID, NodeThread> allInstances;
    private Logger log = Logger.getLogger(Network.class.getName());

    /**
     * Initialize a new network layer based on the IDs of the node in the cluster.
     * Note that these node IDs are final
     * @param allID
     */
    public Network(ArrayList<UUID> allID) {
        this(allID, new HashMap<UUID, NodeThread>());
    }

    /**
     * Initialize a new network layer based on the IDs of the node in the cluster and their instances.
     * Note that these node IDs are final
     * @param allID
     */
    public Network(ArrayList<UUID> allID, HashMap<UUID, NodeThread> allInstances) {
        this.allID = allID;
        this.allInstances = allInstances;
    }


    /**
     * Setter for the instances of the nodes in the cluster
     * @param allInstances instances of the nodes of the cluster
     */
    public void setAllInstances(HashMap<UUID, NodeThread> allInstances) {
        this.allInstances = allInstances;
    }

    /**
     * @param msg Message content
     * @return true if the message was sent successfully. False otherwise
     */
    public boolean sendMessage(Message msg)
    {
        if(!allInstances.containsKey(msg.getOriginalID())){
            this.log.error(msg.getOriginalID() + ": Node is currently off");
            return false;
        }
        if(!allInstances.containsKey(msg.getTargetID())){
            this.log.error(msg.getTargetID() + ": Node is currently off");
            return false;
        }
        try {

            Event event = (Event) SimulatorUtils.deserialize(msg.getMessage());
            new Thread(new Runnable() {
            @Override
            public void run() {
                allInstances.get(msg.getTargetID()).onNewMessage(msg.getOriginalID(), event);
            }

        }).start();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * start the simulation in a node by calling onStart method
     * @param nodeID the ID of the node to be removed.
     */
    public void startNode(UUID nodeID) {

        if(!this.allInstances.containsKey(nodeID))
            Simulator.log.debug("Cannot start node " + nodeID + ": node instance was not found");

        allInstances.get(nodeID).onStart();
    }

    /**
     * Stop a node and remove it from the network.
     * @param nodeID the ID of the node to be removed.
     */
    public void stopNode(UUID nodeID) {

        if(!this.allInstances.containsKey(nodeID))
            Simulator.log.debug("Cannot Stop node " + nodeID + ": node instance was not found");

        allInstances.get(nodeID).terminate();
        allInstances.remove(nodeID);
        allID.remove(nodeID);
    }

    /**
     * getter for node Index in the network
     * @param nodeID ID of the node
     * @return the index of the node in the network
     */
    public int getNodeIndex(UUID nodeID)
    {
        if(!this.allID.contains(nodeID))
            return -1;
        return allID.indexOf(nodeID);
    }

    /**
     * associate a node instance to a specific node ID. Note that the node ID should already by
     * presented to the network.
     * @param nodeID ID of the node
     * @param instance the new Instance
     * @return true if ID was found and instance was added successfully. False, otherwise.
     */
    public boolean addInstance(UUID nodeID, NodeThread instance)
    {
        if(!this.allID.contains(nodeID)) {
            Simulator.log.error("Node instance cannot be added to the network: " + "Node ID of ID " + nodeID + " Was not found in the network");
            return false;
        }
        allInstances.put(nodeID,instance);
        return true;
    }
}
