package Underlay;

import Simulator.Simulator;
import org.apache.log4j.Logger;
import Underlay.packets.Request;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Represents the underlay layer of the simulator
 */

public abstract class Underlay {

    private MiddleLayer middleLayer;

    private int port;
    private String address;
    private String fullAddress;
    protected static final Logger log  = Logger.getLogger(Simulator.class.getName());


    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    /**
     * Dispatches a request to the middle layer
     */
    public void dispatchRequest(Request request) {
        middleLayer.receive(request);
    }

    /**
     * Initializes the underlay.
     * @param port the port that the underlay should be bound to.
     * @param middleLayer
     * @return true iff the initialization was successful.
     */
    public final boolean initialize(int port, MiddleLayer middleLayer) {
        this.port = port;
        this.middleLayer = middleLayer;
        try {
            address = Inet4Address.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {

            log.error("[Underlay] Could not acquire the local host name during initialization.");
            log.error(e.getMessage());
            return false;
        }
        fullAddress = address + ":" + port;
        return initUnderlay(port);
    }

    /**
     * Contains the underlay-specific initialization procedures.
     * @param port the port that the underlay should be bound to.
     * @return true iff the initialization was successful.
     */
    protected abstract boolean initUnderlay(int port);

    /**
     * Can be used to send a request to a remote server that runs the same underlay architecture.
     * @param address address of the remote server.
     * @param port port of the remote server.
     * @param request the request.
     * @return True in case of success, False otherwise
     */
    public abstract boolean sendMessage(String address, int port, Request request);


    /**
     * Terminates the node.
     * @return true iff the termination was successful.
     */
    public abstract boolean terminate(String address, int port);
    
}
