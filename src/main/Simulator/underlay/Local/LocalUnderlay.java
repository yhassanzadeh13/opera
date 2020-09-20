package underlay.Local;

import Node.NodeThread;
import Simulator.*;
import org.apache.log4j.Logger;
import underlay.MiddleLayer;
import underlay.packets.Event;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import underlay.Underlay;
import underlay.packets.Request;


/**
 * Serves as the LocalUnderlay layer of the simulator
 */

public class LocalUnderlay extends Underlay{

    private final HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers;
    private HashMap<SimpleEntry<String, Integer>, Boolean> isReady;
    ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Initialize a new network layer based on the full addresses of the node in the cluster and their instances.
     * Note that these node IDs are final
     * @param allMiddleLayers hashmaps that maps the full addresses to their midlayers
     */
    public LocalUnderlay(HashMap<SimpleEntry<String, Integer>, MiddleLayer> allMiddleLayers, HashMap<SimpleEntry<String, Integer>, Boolean> isReady) {
        this.allMiddleLayers = allMiddleLayers;
        this.isReady = isReady;
    }

    @Override
    public boolean terminate(String address, int port) {
        this.isReady.put(new SimpleEntry<>(address, port), false);
        return true;
    }

    @Override
    protected boolean initUnderlay(int port) {
        return true;
    }

    /**
     *
     * @param address address of the remote server.
     * @param port port of the remote server.
     * @param request the request.
     * @return response for the given request. Null in case of failure
     */
    @Override
    public boolean sendMessage(String address, int port, Request request) {
        SimpleEntry fullAddress = new SimpleEntry<>(address, port);
        if(!isReady.get(fullAddress)){
            log.debug("[LocalUnderlay] " + fullAddress + ": Node is not ready");
            return false;
        }
        if(!allMiddleLayers.containsKey(fullAddress)){
            log.error("[LocalUnderlay] " + address + ": Node is not found");
            return false;
        }
        try {
            MiddleLayer destinationMidLayer = allMiddleLayers.get(fullAddress);

            // handle the request in a separated thread
            Thread handlerThread = new Thread(new LocalHandler(request, destinationMidLayer));
            handlerThread.start();
            return true;
        }catch (NullPointerException e)
        {
            log.error("[LocalUnderlay] Middle layer instance not found ");
            return false;
        }
    }

//    /**
//     * dispatch a received request to the corresponding midlayer
//     * @param request the request.
//     * @return
//     */
//    @Override
//    public void dispatchRequest(Request request) {
//        try {
//            SimpleEntry<String, Integer> destinationFullAddress = allFUllAdresses.get(request.getDestinationID());
//            MiddleLayer destinationMidLayer = allMiddleLayers.get(destinationFullAddress);
//            return destinationMidLayer.receive(request);
//        }
//        catch (NullPointerException e)
//        {
//            this.log.error("Midlayer Instance cannot be found");
//            this.log.error(e.getMessage());
//            return null;
//        }
//
//    }

//    /**
//     * start the simulation in a node by calling onStart method
//     * @param nodeID the ID of the node to be removed.
//     */
//    public void startNode(UUID nodeID) {
//
//    }
//
//    /**
//     * Stop a node and remove it from the network.
//     * @param nodeID the ID of the node to be removed.
//     */
//    public void stopNode(UUID nodeID) {
//        if(!this.allInstances.containsKey(nodeID))
//            Simulator.log.debug("Cannot Stop node " + nodeID + ": node instance was not found");
//
//        lock.writeLock().lock();
//        Simulator.getLogger().debug("accessing allInstances was locked");
//        try {
//            allInstances.get(nodeID).terminate();
//            allInstances.remove(nodeID);
//            allID.remove(nodeID);
//        }finally {
//            Simulator.getLogger().debug("accessing allInstances was released");
//            lock.writeLock().unlock();
//        }
//    }


    /**
     * associate a middle layer to a specific node.
     * @param address address of the node
     * @param port ID of the node
     * @param middleLayer middle layer
     * @return true if ID was found and instance was added successfully. False, otherwise.
     */
    public boolean addInstance(String address, int port, MiddleLayer middleLayer)
    {
        allMiddleLayers.put(new SimpleEntry<>(address, port), middleLayer);
        return true;
    }
}
