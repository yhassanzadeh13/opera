package SkipGraph;

import Node.BaseNode;
import SkipGraph.packets.Request;
import SkipGraph.packets.RequestEvent;
import SkipGraph.packets.Response;
import SkipGraph.packets.ResponseEvent;
import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides a request-response model over the distributed simulator.
 */
public class RequestResponseLayer implements BaseNode {

    private MiddleLayer network;
    private SkipGraphNode overlay;
    private final Map<Integer, FlowMonitor> flowMonitors = new HashMap<>();

    // Fixture constructor.
    public RequestResponseLayer() {}

    public RequestResponseLayer(UUID uuid, MiddleLayer network) {
        this.network = network;
        this.overlay = new SkipGraphNode(uuid, this);
    }

    public MiddleLayer getNetwork() {
        return network;
    }

    /**
     * Invoked by the overlay to send a request to another peer.
     * @param destination the UUID of the destination node.
     * @param request the request to send.
     * @return the response that was received.
     */
    public Response sendRequest(UUID destination, Request request) {
        // Create a monitor for the flow.
        FlowMonitor monitor = new FlowMonitor();
        flowMonitors.put(request.flowId, monitor);
        // Send the request.
        boolean sendRes = network.send(destination, new RequestEvent(request));
        // If the transmission is unsuccessful..
        if(!sendRes) {
            // Remove the invalid flow.
            synchronized (this) {
                this.flowMonitors.remove(request.flowId);
            }
            System.err.println("RequestResponseLayer: Sending failed...");
            return null;
        }
        // Wait for response & return it.
        return monitor.waitForResponse();
    }

    /**
     * Triggered by a ResponseEvent, when a response is received.
     * @param response the response received.
     */
    public synchronized void receiveResponse(Response response) {
        this.flowMonitors.get(response.flowId).dispatchResponse(response);
        this.flowMonitors.remove(response.flowId);
        System.out.println("Flow " + response.flowId + " is removed!");
    }

    /**
     * Triggered by a RequestEvent, when a request is received.
     * @param origin the UUID of the request sender (i.e., client)
     * @param request the request.
     */
    public void receiveRequest(UUID origin, Request request) {
        // Let the overlay handle the request and produce a response.
        Response resp = overlay.handleRequest(request);
        // Set the flow id just in case.
        resp.flowId = request.flowId;
        this.network.send(origin, new ResponseEvent(resp));
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {
        UUID selfId = overlay.getID();
        // Dispatch the ResponseEvent and RequestEvent.
        if(msg instanceof ResponseEvent && network != null) {
            Response resp = ((ResponseEvent) msg).response;
            System.out.println(selfId + " received a response from " + originID + " with flow id: " + resp.flowId);
            receiveResponse(resp);
        } else if(msg instanceof RequestEvent && network != null) {
            Request req = ((RequestEvent) msg).request;
            System.out.println(selfId + " received a request from " + originID + " with flow id: " + req.flowId);
            receiveRequest(originID, req);
        }
    }

    @Override
    public BaseNode newInstance(UUID selfID, MiddleLayer network) {
        return new RequestResponseLayer(selfID, network);
    }

    @Override
    public void onCreate(ArrayList<UUID> allID) {
        // Delegate to the overlay.
        overlay.onCreate(allID);
    }

    @Override
    public void onStart() {
        // Delegate to the overlay.
        overlay.onStart();
    }

    @Override
    public void onStop() {
        // Delegate to the overlay.
        overlay.onStop();
    }
}
