package Simulator;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import Node.BaseNode;
import Utils.Distribution.BaseDistribution;
import Utils.Distribution.UniformDistribution;
import Utils.SimpleEntryComparable;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import underlay.MiddleLayer;
import underlay.Underlay;
import underlay.UnderlayFactory;
import java.util.AbstractMap.SimpleEntry;
import underlay.packets.Event;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class Simulator<T extends BaseNode> implements BaseNode{

    private final static String MOCK_NETWORK = "mockNetwork";

    //Simulator config
    private final int START_PORT = 2000;
    private boolean isLocal;
    private ArrayList<UUID> allID;
    private HashMap<UUID, SimpleEntry<String, Integer>> allFullAddresses;
    private HashMap<SimpleEntry<String, Integer>, Boolean> isReady;
    private T factory;
    public static Logger log = Logger.getLogger(Simulator.class.getName());
    private HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;
    private PriorityQueue<SimpleEntryComparable<Long, UUID>>onlineNodes = new PriorityQueue<>();
    private ArrayList<UUID>offlineNodes = new ArrayList<>();
    private CountDownLatch count;
    private String networkType;
    private static Random rand = new Random();

    @Override
    public void onCreate(ArrayList<UUID> allID) { }

    @Override
    public void onStart() {
        try {
            // initialize prometheus HTTP server
            HTTPServer server = new HTTPServer(this.START_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // logging
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        log.info("New simulation started on " +  dtf.format(now));

        // wait until all nodes are ready
        try{
            count.await();
        }
        catch (Exception e){
            log.error("[Simulator] Count down latch could not wait " + e.getMessage());
        }

        // start all nodes in new threads
        for(MiddleLayer middleNetwork : this.allMiddleLayers.values()){
            middleNetwork.start();
        }
    }

    @Override
    public void onStop() {
        log.info("Nodes will be terminated....");

        //terminating all nodes
        while(!onlineNodes.isEmpty())
        {
            this.Done(onlineNodes.poll().getValue());
        }
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {

        // TODO make the communication between the nodes and the master nodes through the underlay
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
        this.networkType = networkType;

        isReady = new HashMap<SimpleEntry<String, Integer>, Boolean>();

        // generate new IDs and addresses
        this.allID = generateIDs(N);
        this.allFullAddresses = generateFullAddressed(N, this.START_PORT + 1);

        // logging nodes ID
        log.info("Nodes IDs are:");
        for(UUID id : this.allID)
            log.info(id);

        // logging nodes full addresses
        log.info("Nodes Addresses are:");
        for(SimpleEntry<String, Integer> address : this.allFullAddresses.values())
            log.info(address.getKey() + ":" + address.getValue());

        // CountDownLatch for awaiting the start of the simulation until all nodes are ready
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
        // logging
        log.info("Generating full Addresses for " + N + " node..");

        HashMap<UUID, SimpleEntry<String, Integer>> tmp = new HashMap<>();
        try {
            String address = Inet4Address.getLocalHost().getHostAddress();
            for(int i = 0;i < N;i++)
                tmp.put(allID.get(i), new SimpleEntry<>(address, start_port + i));

        } catch(UnknownHostException e) {

            log.error("[Simulator] Could not acquire the local host name during initialization.");
            e.printStackTrace();
        }
        return tmp;
    }


    /**
     * Generate new instances for the nodes and add them to the network
     * @param N number of nodes
     */
    private void generateNodesInstances(int N, String networkType) {

        this.allMiddleLayers = new HashMap<SimpleEntry<String, Integer>, MiddleLayer>();
        //logging
        log.debug("[Simulator] Generating new nodes instances");

        // generate nodes, and middle layers instances
        for(UUID id : allID)
        {
            isReady.put(this.allFullAddresses.get(id), false);
            MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, this);
            BaseNode node = factory.newInstance(id, middleLayer);
            middleLayer.setOverlay(node);
            this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
        }

        // generate new underlays and assign them to the nodes middles layers.
        // in case of the mock network, use the same underlay for all nodes.
        Underlay localUnderlay = UnderlayFactory.getMockUnderlay(this.allMiddleLayers, this.isReady);
        for(Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node : this.allMiddleLayers.entrySet())
        {
            MiddleLayer middleLayer = node.getValue();
            int port = node.getKey().getValue();
            if(!networkType.equals(MOCK_NETWORK))
                middleLayer.setUnderlay(UnderlayFactory.NewUnderlay(networkType, port, middleLayer));
            else{
                middleLayer.setUnderlay(localUnderlay);
            }
            // call the node onCreat method of the nodes
            middleLayer.create(this.allID);
        }
    }

    /**
     * Should be called by the node to declare itself ready for simulation.
     * @param nodeID ID of the node
     */
    public void Ready(UUID nodeID)
    {
        isReady.put(this.allFullAddresses.get(nodeID), true);

        // logging
        log.info(nodeID + ": node is ready");

        // start the nodes directly if the simulation is running, or wait for all nodes to be ready in case otherwise.
        if(count.getCount() <= 0){
            this.getMiddleLayer(nodeID).start();
        }
        else {
            count.countDown();
        }
    }

    /**
     * Should be called by the node when it is done with the simulation and want to terminate
     * @param nodeID ID of the node
     */
    public void Done(UUID nodeID)
    {
        // logging
        log.info(getAddress(nodeID) + ": node is terminating...");

        // mark the nodes as not ready
        isReady.put(this.allFullAddresses.get(nodeID), false);
        SimpleEntry<String, Integer> fullAddress = allFullAddresses.get(nodeID);

        // stop the nodes on a new thread
        try{
            MiddleLayer middleLayer = this.allMiddleLayers.get(fullAddress);
            middleLayer.stop(fullAddress.getKey(), fullAddress.getValue());
        }catch (NullPointerException e){
            log.error("[Simulator] Cannot find node " + getAddress(nodeID));
            log.debug("[Simulator] Node " + getAddress(nodeID) + " has already been terminate");
        }

        // add the offline nodes list
        this.offlineNodes.add(nodeID);

        // logging
        log.info(getAddress(nodeID) + ": node has been terminated");
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
        churnSimulation(duration, new UniformDistribution(2000, 3000), new UniformDistribution(2000, 3000));
        log.info("Simulation duration finished");
        System.out.println("Simulation duration finished");

        // stop the simulation.
        this.onStop();
    }

    /**
     * Simulate churn based on inter-arrival time and session length
     * @param duration duration of the simulator
     * @param arrival inter-arrival
     * @param session session length
     */

    private void churnSimulation(long duration, BaseDistribution arrival, BaseDistribution session){
        long time = System.currentTimeMillis();

        // hold the current online nodes, with their termination time stamp.
        this.onlineNodes = new PriorityQueue<>();

        // assign initial terminate time stamp for all nodes
        for(UUID id : this.allID){
            if(isReady.get(this.allFullAddresses.get(id))){
                int ex = session.next();
                log.info("[Simulator] new session for node " + getAddress(id)  + ": " + ex + " ms");
                onlineNodes.add(new SimpleEntryComparable<>(time + ex, id));
            }
        }

        // hold next arrival time
        long nxtArrival = System.currentTimeMillis() + arrival.next();

        while(System.currentTimeMillis() - time < duration){
            if(!onlineNodes.isEmpty() && System.currentTimeMillis() > onlineNodes.peek().getKey()){
                // terminate the node with the nearest termination time (if the time met)
                // `Done` will add the node to the offline nodes
                UUID id = onlineNodes.poll().getValue();
                log.info("[Simulator] Deactivating node " + getAddress(id));
                this.Done(id);
            }
            if(System.currentTimeMillis() >= nxtArrival){
                // pool a random node from the offline nodes (if exists) and start it in a new thread.
                if(this.offlineNodes.isEmpty()) continue;

                int ind = rand.nextInt(this.offlineNodes.size());
                UUID id = this.offlineNodes.get(ind);
                log.info("[Simulator] Activating node " + getAddress(id));
                this.offlineNodes.remove(ind);

                // creat the new node in a new thread
                // Once the node call `ready` method, the node's onStart method will be called
                MiddleLayer middleLayer = this.getMiddleLayer(id);
                middleLayer.initUnderLay();
                middleLayer.create(this.allID);

                // assign a termination time
                int ex = session.next();
                log.info("[Simulator] new session for node " + getAddress(id)  + ": " + ex + " ms");
                this.onlineNodes.add(new SimpleEntryComparable<>(System.currentTimeMillis() + ex, id));

                // assign a next node arrival time
                nxtArrival = System.currentTimeMillis() + arrival.next();
                log.info("[Simulator] next node arrival: " +  nxtArrival);
            }
        }
    }

    public String getAddress(UUID nodeID){
        SimpleEntry<String, Integer> address = allFullAddresses.get(nodeID);
        return address.getKey() + ":" + address.getValue();
    }

    public MiddleLayer getMiddleLayer(UUID id){
        SimpleEntry<String, Integer> address = this.allFullAddresses.get(id);
        return this.allMiddleLayers.get(address);
    }

}
