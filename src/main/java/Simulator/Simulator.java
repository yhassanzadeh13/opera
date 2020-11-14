package Simulator;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.Local.LocalUnderlay;
import Utils.Generator.BaseGenerator;
import Utils.Generator.GaussianGenerator;
import Utils.SimpleEntryComparable;
import Utils.SimulatorUtils;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import Underlay.MiddleLayer;
import Underlay.UnderlayFactory;
import java.util.AbstractMap.SimpleEntry;
import Underlay.packets.Event;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class Simulator<T extends BaseNode> implements BaseNode{
    //Simulator.Simulator config
    private final static String MOCK_NETWORK = "mockNetwork";
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
    private static UUID SimulatorID = UUID.randomUUID();
    private HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
    public HashMap<String, Integer> nodesSimulatedLatency = new HashMap<>();


    // TODO: currently the communication is assumed to be on a single machine.
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

        // prepare the prometheus server
        SimulatorUtils.ConfigurePrometheus("admin", "admin");

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
            this.generateNodesInstances(networkType);
        }
    }

    @Override
    public void onCreate(ArrayList<UUID> allID) { }

    @Override
    public void onStart() {
        // logging
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        log.info("New simulation started on " +  dtf.format(now));

        // wait until all nodes are ready
        try{
            count.await();
        }
        catch (Exception e){
            log.error("[Simulator.Simulator] Count down latch could not wait " + e.getMessage());
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

            log.error("[Simulator.Simulator] Could not acquire the local host name during initialization.");
            e.printStackTrace();
        }
        return tmp;
    }


    /**
     * Generate new instances for the nodes and add them to the network
     */
    private void generateNodesInstances(String networkType) {

        this.allMiddleLayers = new HashMap<SimpleEntry<String, Integer>, MiddleLayer>();
        //logging
        log.debug("[Simulator.Simulator] Generating new nodes instances");

        // generate nodes, and middle layers instances
        for(UUID id : allID)
        {
            isReady.put(this.allFullAddresses.get(id), false);
            MiddleLayer middleLayer = new MiddleLayer(id, this.allFullAddresses, isReady, this);
            BaseNode node = factory.newInstance(id, middleLayer);
            middleLayer.setOverlay(node);
            this.allMiddleLayers.put(this.allFullAddresses.get(id), middleLayer);
        }

        // generate new underlays and assign them to the nodes middles layers.
        for(Map.Entry<SimpleEntry<String, Integer>, MiddleLayer> node : this.allMiddleLayers.entrySet())
        {
            MiddleLayer middleLayer = node.getValue();
            String address = node.getKey().getKey();
            int port = node.getKey().getValue();
            if(!networkType.equals(MOCK_NETWORK))
                middleLayer.setUnderlay(UnderlayFactory.NewUnderlay(networkType, port, middleLayer));
            else{
                LocalUnderlay underlay = UnderlayFactory.getMockUnderlay(address, port, middleLayer, allLocalUnderlay);
                middleLayer.setUnderlay(underlay);
                allLocalUnderlay.put(node.getKey(), underlay);
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
            log.error("[Simulator.Simulator] Cannot find node " + getAddress(nodeID));
            log.debug("[Simulator.Simulator] Node " + getAddress(nodeID) + " has already been terminate");
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
    public void constantSimulation(int duration)
    {
        this.onStart();
        System.out.println("Simulation started");
        getLogger().info("Simulation started");

        try{
            Thread.sleep(duration);
        }
        catch (Exception e){
         e.printStackTrace();
        }

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

    public void churnSimulation(long duration, BaseGenerator arrival, BaseGenerator session){
        final String SESSION_METRIC = "SessionLength";
        final String ARRIVAL_METRIC = "InterArrival";

        // initialize all nodes
        this.onStart();
        System.out.println("Simulation started");
        getLogger().info("Simulation started");

        // register a prometheus histogram for session length
        double[] labels = new double[10];
        for(int i = 1;i<=10;i++){
            labels[10 - i] = (double) (session.mx - session.mn) / i;
        }
        SimulatorHistogram.register(SESSION_METRIC, labels);

        // register a prometheus histogram for inter arrival time
        for(int i = 1;i<=10;i++){
            labels[10 - i] = (double) (arrival.mx - arrival.mn) / i;
        }
        SimulatorHistogram.register(ARRIVAL_METRIC, labels);

        // hold the current online nodes, with their termination time stamp.
        this.onlineNodes = new PriorityQueue<>();

        // assign initial terminate time stamp for all nodes
        long time = System.currentTimeMillis();
        for(UUID id : this.allID){
            if(isReady.get(this.allFullAddresses.get(id))){
                int ex = session.next();
                log.info("[Simulator.Simulator] new session for node " + getAddress(id)  + ": " + ex + " ms");
                onlineNodes.add(new SimpleEntryComparable<>(time + ex, id));
                SimulatorHistogram.observe(SESSION_METRIC, id, ex);
            }
        }
        // hold next arrival time
        long nxtArrival = System.currentTimeMillis() + arrival.next();
        SimulatorHistogram.observe(ARRIVAL_METRIC, SimulatorID, nxtArrival);

        while(System.currentTimeMillis() - time < duration){
            if(!onlineNodes.isEmpty() && System.currentTimeMillis() > onlineNodes.peek().getKey()){
                // terminate the node with the nearest termination time (if the time met)
                // `Done` will add the node to the offline nodes
                UUID id = onlineNodes.poll().getValue();
                log.info("[Simulator.Simulator] Deactivating node " + getAddress(id));
                this.Done(id);
            }
            if(System.currentTimeMillis() >= nxtArrival) {
                // pool a random node from the offline nodes (if exists) and start it in a new thread.
                if (this.offlineNodes.isEmpty()) continue;

                int ind = rand.nextInt(this.offlineNodes.size());
                UUID id = this.offlineNodes.get(ind);
                log.info("[Simulator.Simulator] Activating node " + getAddress(id));
                this.offlineNodes.remove(ind);

                // creat the new node in a new thread
                // Once the node call `ready` method, the node's onStart method will be called
                MiddleLayer middleLayer = this.getMiddleLayer(id);
                middleLayer.initUnderLay();
                middleLayer.create(this.allID);

                // assign a termination time
                int ex = session.next();
                log.info("[Simulator.Simulator] new session for node " + getAddress(id) + ": " + ex + " ms");
                this.onlineNodes.add(new SimpleEntryComparable<>(System.currentTimeMillis() + ex, id));
                SimulatorHistogram.observe(SESSION_METRIC, id, ex);

                // assign a next node arrival time
                nxtArrival = System.currentTimeMillis() + arrival.next();
                log.info("[Simulator.Simulator] next node arrival: " + nxtArrival);
                SimulatorHistogram.observe(ARRIVAL_METRIC, SimulatorID, nxtArrival);
            }
        }

        log.info("Simulation duration finished");
        System.out.println("Simulation duration finished");

        // stop the simulation.
        this.onStop();
    }

    public String getAddress(UUID nodeID){
        SimpleEntry<String, Integer> address = allFullAddresses.get(nodeID);
        return address.getKey() + ":" + address.getValue();
    }

    public MiddleLayer getMiddleLayer(UUID id){
        SimpleEntry<String, Integer> address = this.allFullAddresses.get(id);
        return this.allMiddleLayers.get(address);
    }

    /**
     * get the simulated delay based on the normal distribution extracted from the AWS.
     * @param nodeA first node
     * @param nodeB second node
     * @return
     */
    public int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional){
        if(bidirectional && nodeA.compareTo(nodeB) < 0){
            UUID tmp = nodeA;
            nodeA = nodeB;
            nodeB = tmp;
        }
        String hash = SimulatorUtils.hashPairOfNodes(nodeA, nodeB);
        if(!this.nodesSimulatedLatency.containsKey(hash)){
            final int MEAN = 159;
            final int STD = 96;
            GaussianGenerator generator = new GaussianGenerator(MEAN, STD);
            this.nodesSimulatedLatency.put(hash, generator.next());
        }
        return this.nodesSimulatedLatency.get(hash);
    }

    /** get all nodes ID **/
    public ArrayList<UUID> getAllID(){
        return this.allID;
    }

}
