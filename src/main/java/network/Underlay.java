package network;

import java.net.InetSocketAddress;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.exception.OperaNetworkingException;
import network.model.Message;
import node.Identifier;

/**
 * Represents the underlay layer of the simulator.
 */

public abstract class Underlay {
  protected Network network; // TODO: make this final.

  /**
   * Sends a message to a remote node.
   *
   * @param targetAddress address of the remote node who should receive the message.
   * @param message       the message to be sent.
   * @throws OperaNetworkingException if it could not send the message.
   */
  public abstract void send(InetSocketAddress targetAddress, Message message) throws OperaNetworkingException;

  public abstract int getPort();

  public abstract String getAddress();

  public String getFullAddress() {
    return getAddress() + ":" + getPort();
  }

  /**
   * Dispatches a request to the middle layer.
   */
  public void dispatchRequest(Message request) {
    network.receive(request);
  }

  /**
   * Initializes the underlay.
   *
   * @param port    the port that the underlay should be bound to.
   * @param network middle layer for underlay.
   * @return true iff the initialization was successful.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public final boolean initialize(int port, Network network) {
    this.network = network;
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
   * Terminates the node.
   *
   * @throws IllegalStateException if it could not terminate the node.
   */
  // TODO: add timeout
  public abstract void terminate() throws IllegalStateException;

  public Identifier getNodeId() {
    return network.getNodeId();
  }
}
