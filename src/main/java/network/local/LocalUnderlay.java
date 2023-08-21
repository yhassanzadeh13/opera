package network.local;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.Underlay;
import network.model.Message;


/**
 * Serves as the LocalUnderlay layer of the simulator.
 */

public class LocalUnderlay extends Underlay {
  InetSocketAddress selfAddress;
  // TODO: replace it with a network Hub.
  private final HashMap<InetSocketAddress, LocalUnderlay> allUnderlay;

  /**
   * Constructor of LocalUnderlay.
   *
   * @param selfAddress Address of the underlay
   * @param port        port of the Underlay
   * @param allUnderlay hashmap of all underlays
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allUnderlays")
  public LocalUnderlay(InetSocketAddress selfAddress, HashMap<InetSocketAddress, LocalUnderlay> allUnderlay) {
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
    if (this.selfAddress.getPort() > 0) {
      throw new IllegalStateException("Underlay is already initialized");
    }
    this.selfAddress = new InetSocketAddress(this.selfAddress.getAddress(), port);
    allUnderlay.put(this.selfAddress, this);
    return true;
  }

  /**
   * Sends a request to Underlay. Return true if no errors.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request the request.
   * @return response for the given request. Null in case of failure
   */
  @Override
  public boolean sendMessage(String address, int port, Message request) {
    SimpleEntry<String, Integer> fullAddress = new SimpleEntry<>(address, port);
    if (!allUnderlay.containsKey(fullAddress)) {
      // TODO: throw illegal state exception.
      return false;
    }

    Underlay destinationUnderlay = allUnderlay.get(fullAddress);

    // handle the request in a separated thread
    new Thread() {
      @Override
      public void run() {
        destinationUnderlay.dispatchRequest(request);
      }
    }.start();
    return true;
  }

  /**
   * associate a middle layer to a specific node.
   *
   * @param address  address of the node
   * @param port     identifier of the node
   * @param underlay underlay to add.
   * @return true if identifier was found and instance was added successfully. False, otherwise.
   */
  public boolean addInstance(InetSocketAddress address, LocalUnderlay underlay) {
    allUnderlay.put(address, underlay);
    return true;
  }
}
