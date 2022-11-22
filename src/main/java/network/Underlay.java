package network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.packets.Request;
import node.Identifier;

/**
 * Represents the underlay layer of the simulator.
 */

public abstract class Underlay {
  protected MiddleLayer middleLayer; // TODO: make this final.

  public abstract int getPort();

  public abstract String getAddress();

  public String getFullAddress() {
    return getAddress() + ":" + getPort();
  }

  /**
   * Dispatches a request to the middle layer.
   */
  public void dispatchRequest(Request request) {
    middleLayer.receive(request);
  }

  /**
   * Initializes the underlay.
   *
   * @param port        the port that the underlay should be bound to.
   * @param middleLayer middle layer for underlay.
   * @return true iff the initialization was successful.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public final boolean initialize(int port, MiddleLayer middleLayer) {
    this.middleLayer = middleLayer;
    return initUnderlay(port);
  }

  /**
   * Contains the underlay-specific initialization procedures.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff the initialization was successful.
   */
  protected abstract boolean initUnderlay(int port);

  /**
   * Can be used to send a request to a remote server that runs the same underlay architecture.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request the request.
   * @return True in case of success, False otherwise
   */
  public abstract boolean sendMessage(String address, int port, Request request);


  /**
   * Terminates the node.
   *
   * @throws IllegalStateException if it could not terminate the node.
   */
  // TODO: add timeout
  public abstract void terminate() throws IllegalStateException;

  public Identifier getNodeId() {
    return middleLayer.getNodeId();
  }
}
