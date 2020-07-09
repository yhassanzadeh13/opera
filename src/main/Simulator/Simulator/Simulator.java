package Simulator;

import java.util.*;

import org.apache.log4j.Logger;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Simulator<T extends BaseNode> {

    //Simulator config
    private boolean isLocal = true;
    private ArrayList<UUID> allID;
    private static HashMap<UUID, Boolean> isReady;
    private T factory;
    private static Network network;
    static Logger log = Logger.getLogger(Simulator.class.getName());

    /**
     * Initializes a newly created simulator based on a factory node and the number of nodes in
     * the simulator.
     * @param factory a dummy factory instance of special node class.
     * @param N the number of nodes.
     */
    public Simulator(T factory, int N)
    {
        this(factory, N, true);
    }

    /**
     * Constructors to be added later when the online simulation feature is added.
     * @param factory
     * @param N
     * @param isLocal
     */
    private Simulator(T factory, int N, boolean isLocal)
    {
        this(factory, N, isLocal, new ArrayList<UUID>());
    }

    private Simulator(T factory, int N, boolean isLocal, ArrayList<UUID> allID)
    {
        this.factory = factory;
        this.isLocal = isLocal;
        this.allID = allID;

        isReady = new HashMap<UUID, Boolean>();

        this.allID = generateIDs(N);

        //logging
        log.info("Nodes IDs are:");
        for(UUID id : this.allID)
            log.info(id);


        network = new Network(this.allID);
        if(isLocal)
        {
            this.generateNodesInstances(this.allID);
        }
    }

    /**
     * Generate new random UUID for the nodes
     * @param N number of nodes
     * @return ArrayList of random N ids
     */
    private ArrayList<UUID> generateIDs(int N)
    {
        //logging
        log.info("Generating IDs for " + N + " node..");

        ArrayList<UUID> tmp = new ArrayList<>();
        for(int i = 0;i < N; i++)
            tmp.add(UUID.randomUUID());

        return tmp;
    }

    /**
     * Generate new instances for the nodes and add them to the network
     * @param allID ArrayList of the IDs of the nodes
     */
    private void generateNodesInstances(ArrayList<UUID> allID) {

        //logging
        log.debug("Generating nodes instanes");

        for(int i = 0; i < allID.size(); i++)
        {
            isReady.put(allID.get(i), false);
            NodeThread<T> node = new NodeThread<T>(factory, allID.get(i), allID);
            network.addInstance(allID.get(i), node);
            node.onCreate(allID);
        }
    }


    /**
     * Serves as a channel for nodes to communicate. When a node A want to send a new message
     * to node B it should call this method.
     * @param originalID the sender node ID
     * @param targetID the receiver node ID
     * @param msg the message content
     * @return
     */
    public static boolean Submit(UUID originalID, UUID targetID, Event msg)
    {
        if(!isReady.get(originalID)){
            Simulator.log.error(originalID + ": Node is not ready yet..");
            return false;
        }
        if(!isReady.get(targetID)){
            Simulator.log.error(targetID + ": Node is not ready yet..");
            return false;
        }
        try{
            byte[] tmp = SimulatorUtils.serialize(msg);
            boolean sent =  network.sendMessage(new Message(originalID, targetID, tmp));
            if(sent)
            {
                log.info(originalID + ": an event " + msg.logMessage() + " was sent to node " + targetID);
                return true;
            }
            else
                log.error(originalID + ": event " + msg.logMessage() + " was not able to be sent to node " + targetID);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Should be called by the node to declare itself ready for simulation.
     * @param nodeID ID of the node
     */
    public static void Ready(UUID nodeID)
    {
        isReady.put(nodeID, true);

        //logging
        log.info(nodeID + ": node is ready");
    }

    /**
     * Should be called by the node when it is done with the simulation and want to terminate
     * @param nodeID ID of the node
     */
    public static void Done(UUID nodeID)
    {
        //logging
        log.info(nodeID + ": node is terminating...");

        isReady.put(nodeID, false);
        network.stopNode(nodeID);

        //logging
        log.info(nodeID + ": node has been terminated");
    }


    /**
     * getter for node Index in the Simulator
     * @param nodeID ID of the node
     * @return the index of the node in the network
     */
    public static int getNodeIndex(UUID nodeID)
    {
        return network.getNodeIndex(nodeID);
    }

    /**
     * Used to start the simulation.
     * It calls the onStart method for all nodes to start the simulation.
     */
    public void start()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        log.info("New simulation started on " +  dtf.format(now));
        for(UUID id : this.allID)
            network.startNode(id);
    }

}
