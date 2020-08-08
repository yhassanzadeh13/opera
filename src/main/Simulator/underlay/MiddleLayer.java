package underlay;
import Node.BaseNode;
import Simulator.Simulator;
import SimulatorEvents.ReadyEvent;
import SimulatorEvents.StopStartEvent;
import javafx.util.Pair;
import underlay.Underlay;
import underlay.packets.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a mediator between the overlay and the underlay. The requests coming from the underlay are directed
 * to the overlay and the responses emitted by the overlay are returned to the underlay. The requests coming from
 * the overlay are either directed to the underlay or to another local overlay, and the emitted response is returned
 * to the overlay.
 */
public class MiddleLayer {

    private Underlay underlay;
    private BaseNode overlay;
    private HashMap<UUID, Pair<String, Integer>> allFUllAddresses;
    private UUID nodeID;

    // TODO : make the communication between the nodes and the simulator (the master node) through the network
    private Simulator masterNode;

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

    public MiddleLayer(UUID nodeID, HashMap<UUID, Pair<String, Integer>> allFUllAdresses, Simulator masterNode) {
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
        // wrap the even by request class
        Request request = new Request(event, this.nodeID, destinationID);
        Pair<String, Integer> fullAddress = this.allFUllAddresses.get(destinationID);
        String destinationAddress = fullAddress.getKey();
        Integer port = fullAddress.getValue();
        // Bounce the request up.
        return underlay.sendMessage(destinationAddress, port, request);
    }

    /**
     * Called by the underlay to collect the response from the overlay.
     */
    public void receive(Request request) {
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
