package Simulator;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import Node.BaseNode;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import underlay.Local.LocalUnderlay;
import underlay.MiddleLayer;
import underlay.TCP.TCPUnderlay;
import underlay.UDP.UDPUnderlay;
import underlay.Underlay;
import underlay.UnderlayFactory;
import underlay.javaRMI.JavaRMIUnderlay;
import java.util.AbstractMap.SimpleEntry;
import underlay.packets.Event;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class Simulator<T extends BaseNode> implements BaseNode{

    //Simulator config
    private final int START_PORT = 2000;
    private boolean isLocal;
    private ArrayList<UUID> allID;
    private HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses;
    private HashMap<UUID, Boolean> isReady;
    private T factory;
    public static Logger log = Logger.getLogger(Simulator.class.getName());
    private HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;

    private CountDownLatch count;

    @Override
    public void onCreate(ArrayList<UUID> allID) { }

    @Override
    public void onStart() {
        try {
            HTTPServer server = new HTTPServer(this.START_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        log.info("New simulation started on " +  dtf.format(now));
        try{
            count.await();
        }
        catch (Exception e){
            log.error("Count down latch could not wait " + e.getMessage());
        }

        for(MiddleLayer middleNetwork : this.allMiddleLayers.values()){
            new Thread(){
                public void run(){
                    middleNetwork.start();
                }
            }.start();
        }
    }

    @Override
    public void onStop() {
        log.info("Nodes will be terminated....");
        //terminating all nodes
        while(!allID.isEmpty())
        {
            int sz = allID.size();
            this.Done(allID.get(sz-1));
        }
        Thread.interrupted();
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {
        msg.actionPerformed(this);
    }

    @Override
    public BaseNode newInstance(UUID selfID, MiddleLayer middleLayer) {
        return null;
    }


    /**
     * Initializes a newly created simulator based on a factory node and the number of nodes in
     * the simulator.
     * @param factory a dummy factory instance of special node class.
     * @param N the number of nodes.
     */
    public Simulator(T factory, int N, String networkType)
    {
        this(factory, N, networkType, true);
    }

    /**
     * Constructors to be added later when the online simulation feature is added.
     * @param factory
     * @param N
     * @param isLocal
     */
    private Simulator(T factory, int N, String networkType, boolean isLocal)
    {
        this.factory = factory;
        this.isLocal = isLocal;

        isReady = new HashMap<UUID, Boolean>();

        // generate new IDs and addresses
        this.allID = generateIDs(N);
        this.allFullAddresses = generateFullAddressed(N, this.START_PORT + 1);

        //logging
        log.info("Nodes IDs are:");
        for(UUID id : this.allID)
            log.info(id);

        // logging
        log.info("Nodes Addresses are:");
        for(SimpleEntry<String, Integer> address : this.allFullAddresses.values())
            log.info(address.getKey() + ":" + address.getValue());

        count = new CountDownLatch(N);
        if(isLocal)
        {
            this.generateNodesInstances(N, networkType);
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


    private HashMap<UUID, SimpleEntry<String, Integer>> generateFullAddressed(int N, int start_port){
        //logging
        log.info("Generating full Addresses for " + N + " node..");

        HashMap<UUID, SimpleEntry<String, Integer>> tmp = new HashMap<>();

        String address = "localhost";

        try {
            address = Inet4Address.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {

            log.error("[Underlay] Could not acquire the local host name during initialization.");
            log.error(e.getMessage());
        }

        System.out.println(address);
        for(int i = 0;i < N;i++)
            tmp.put(allID.get(i), new SimpleEntry<>(address, start_port + i));

        return tmp;
    }


    /**
     * Generate new instances for the nodes and add them to the network
     * @param N number of nodes
     */
    private void generateNodesInstances(int N, String networkType) {

        this.allMiddleLayers = new HashMap<SimpleEntry<String, Integer>, MiddleLayer>();
        //logging
        log.debug("Generating new nodes instances");

        for(UUID id : allID)
        {
            isReady.put(id, false);
            MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, this);
            BaseNode node = factory.newInstance(id, middleLayer);
            middleLayer.setOverlay(node);
            this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
        }
        Underlay underlay = UnderlayFactory.getMockUnderlay(this.allMiddleLayers);
        for(Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node : this.allMiddleLayers.entrySet())
        {
            MiddleLayer middleLayer = node.getValue();
            int port = node.getKey().getValue();
            if(!networkType.equals("mockNetwork"))
                underlay = UnderlayFactory.NewUnderlay(networkType, port, middleLayer);
            middleLayer.setUnderlay(underlay);
            // call the node onCreat method
            middleLayer.create(this.allID);
        }
    }

    /**
     * Should be called by the node to declare itself ready for simulation.
     * @param nodeID ID of the node
     */
    public void Ready(UUID nodeID)
    {
        isReady.put(nodeID, true);
        //logging
        log.info(nodeID + ": node is ready");
        count.countDown();
    }

    /**
     * Should be called by the node when it is done with the simulation and want to terminate
     * @param nodeID ID of the node
     */
    public void Done(UUID nodeID)
    {
        //logging
        log.info(nodeID + ": node is terminating...");

        isReady.put(nodeID, false);
        SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(nodeID);
        try{
            MiddleLayer middleLayer = this.allMiddleLayers.get(fullAddress);
            new Thread(){
                public void run(){
                    middleLayer.stop(fullAddress.getKey(), fullAddress.getValue());
                }
            }.start();
            this.allID.remove(nodeID);
        }catch (NullPointerException e){
            log.error("[Simulator] Cannot find node " + nodeID);
            log.debug("[Simulator] Node " + nodeID + "has already been terminate");
        }


        //logging
        log.info(nodeID + ": node has been terminated");
    }

    /**
     * getter for the simulator logger of log4j
     * @return the simultor logger
     */
    public static Logger getLogger()
    {
        return log;
    }


    /**
     * Used to start the simulation.
     * It calls the onStart method for all nodes to start the simulation.
     */
    public void start(int duration)
    {
        this.onStart();
        try{
            Thread.sleep(duration);
        }catch (Exception e)
        {
            log.error(e.getMessage());
        }

        log.info("Simulation duration finished");
        // stop the simulator.
        this.onStop();
    }

}
