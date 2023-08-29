package network.local;

import java.net.InetSocketAddress;
import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.Underlay;
import network.exception.OperaNetworkingException;
import network.model.Message;


/**
 * Serves as the LocalUnderlay layer of the simulator.
 */

public class LocalUnderlay extends Underlay {
  // TODO: replace it with a network Hub.
  /**
   * Hash table of the full addresses of all the nodes in the network.
   */
  private final HashMap<InetSocketAddress, LocalUnderlay> allUnderlay;
  /**
   * Address of the current instance of underlay.
   */
  private InetSocketAddress selfAddress;

  /**
   * Constructor of LocalUnderlay.
   *
   * @param selfAddress Address of the underlay
   * @param allUnderlay hashmap of all underlays
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allUnderlays")
  public LocalUnderlay(final InetSocketAddress selfAddress, final HashMap<InetSocketAddress, LocalUnderlay> allUnderlay) {
    this.selfAddress = selfAddress;
    this.allUnderlay = allUnderlay;
  }

  @Override
  public void terminate() {
  }

  @Override
  public int getPort() {
    return this.getPort();
  }

  @Override
  public String getAddress() {
    return this.selfAddress.getAddress().toString();
  }

  @Override
  protected boolean initUnderlay(int port) {
    this.selfAddress = new InetSocketAddress(this.selfAddress.getAddress(), port);
    allUnderlay.put(this.selfAddress, this);
    return true;
  }

  /**
   * Sends a message to a remote node.
   *
   * @param targetAddress address of the remote node who should receive the message.
   * @param message       the message to be sent.
   * @throws network.exception.OperaNetworkingException if it could not send the message.
   */
  @Override
  public void send(final InetSocketAddress targetAddress, final Message message) throws OperaNetworkingException {
    if (!allUnderlay.containsKey(targetAddress)) {
      throw new OperaNetworkingException(String.format("Could not find underlay for %s", targetAddress));
    }
    Underlay destinationUnderlay = allUnderlay.get(targetAddress);

    // handle the request in a separated thread
    new Thread() {
      @Override
      public void run() {
        destinationUnderlay.dispatchRequest(message);
      }
    }.start();
  }

  /**
   * associate a middle layer to a specific node.
   *
   * @param address  address of the node
   * @param underlay underlay to add.
   * @return true if identifier was found and instance was added successfully. False, otherwise.
   */
  public boolean addInstance(final InetSocketAddress address, final LocalUnderlay underlay) {
    allUnderlay.put(address, underlay);
    return true;
  }
}
