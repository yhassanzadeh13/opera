package underlay;
import Metrics.SimulatorCounter;
import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Simulator.Simulator;
import SimulatorEvents.ReadyEvent;
import SimulatorEvents.StopStartEvent;
import java.util.AbstractMap.SimpleEntry;
import underlay.Underlay;
import underlay.packets.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.lang.instrument.Instrumentation;
/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay are directed
 * to the overlay and the responses emitted by the overlay are returned to the underlay. The requests coming from
 * the overlay are either directed to the underlay or to another local overlay, and the emitted response is returned
 * to the overlay.
 */
public class MiddleLayer {


    private final String DELAY_METRIC = "Delay";
    private final String SENT_BUCKET_SIZE_METRIC = "SentBucketSize";
    private final String RECEIVED_BUCKET_SIZE_METRIC = "ReceivedBucketSize";
    private final String SENT_MSG_CNT_METRIC = "Sent_Messages";
    private final String RECEIVED_MSG_CNT_METRIC = "Received_Messages";

    private Underlay underlay;
    private BaseNode overlay;
    private HashMap<UUID, SimpleEntry<String, Integer>> allFUllAddresses;
    private UUID nodeID;

    // TODO : make the communication between the nodes and the simulator (the master node) through the network
    private Simulator masterNode;

    private String sentBucketHash(UUID id){
        return nodeID.toString() + "->" + id.toString();
    }
    private String receivedBucketHash(UUID id){
        return id.toString() + "->" + nodeID.toString();
    }

    public void setUnderlay(Underlay underlay){
        this.underlay = underlay;
    }

    public void setOverlay(BaseNode overlay){
        this.overlay = overlay;
    }

    public Underlay getUnderlay() {
        return underlay;
    }

    public BaseNode getOverlay() {
        return overlay;
    }

    public MiddleLayer(UUID nodeID, HashMap<UUID, SimpleEntry<String, Integer>> allFUllAdresses, Simulator masterNode) {
        //register metrics
        SimulatorHistogram.register(DELAY_METRIC);
        SimulatorHistogram.register(SENT_BUCKET_SIZE_METRIC);
        SimulatorHistogram.register(RECEIVED_BUCKET_SIZE_METRIC);
        SimulatorCounter.register(SENT_MSG_CNT_METRIC);
        SimulatorCounter.register(RECEIVED_MSG_CNT_METRIC);

        this.nodeID = nodeID;
        this.allFUllAddresses = allFUllAdresses;
        this.masterNode = masterNode;
    }

    /**
     * Called by the overlay to send requests to the underlay.
     * @param destinationID destenation node unique id.
     * @param event the event.
     * @return true if event was sent successfully. false, otherwise.
     */
    public boolean send(UUID destinationID, Event event) {

        // update metrics
        SimulatorCounter.inc(SENT_MSG_CNT_METRIC, nodeID);
        SimulatorHistogram.startTimer(DELAY_METRIC, nodeID, sentBucketHash(destinationID));

        // wrap the even by request class
        Request request = new Request(event, this.nodeID, destinationID);
        SimpleEntry<String, Integer> fullAddress = this.allFUllAddresses.get(destinationID);
        String destinationAddress = fullAddress.getKey();
        Integer port = fullAddress.getValue();
        // Bounce the request up.
        return underlay.sendMessage(destinationAddress, port, request);
    }

    /**
     * Called by the underlay to collect the response from the overlay.
     */
    public void receive(Request request) {
        // update metrics
        SimulatorCounter.inc(RECEIVED_MSG_CNT_METRIC, nodeID);
        SimulatorHistogram.observeDuration(DELAY_METRIC, nodeID, receivedBucketHash(request.getOrginalID()));

        // check if the event is start, stop event and handle it directly
        if (request.getEvent() instanceof StopStartEvent){
            StopStartEvent event = (StopStartEvent) request.getEvent();
            if(event.getState())
                this.start();
            else
                this.stop(event.getAddress(), event.getPort());
        }
        else overlay.onNewMessage(request.getOrginalID(), request.getEvent());
    }

    /**
     * Terminates the node.
     * @return true iff the termination was successful.
     */
    public void stop(String address, int port)
    {
        this.overlay.onStop();
        underlay.terminate(address, port);
    }

    /**
     * start the node. This method will be called once the simulator send a start event to the node
     * @return true iff the node started successfully.
     */
    public void start()
    {
        this.overlay.onStart();
    }

    /**
     * declare the node as Ready (called by the node)
     */
    public void ready(){
        masterNode.Ready(this.nodeID);
    }

    /**
     * request node termination (called by the node)
     */
    public void done(){
        masterNode.Done(this.nodeID);
    }

    public void create(ArrayList<UUID> allID) {
        this.overlay.onCreate(allID);
    }
}
