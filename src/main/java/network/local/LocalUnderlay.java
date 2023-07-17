package network.local;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.Underlay;
import network.model.Message;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;


/**
 * Serves as the LocalUnderlay layer of the simulator.
 */

public class LocalUnderlay extends Underlay {
  private final String selfAddress;
  private final int port;
  // TODO: replace it with a network Hub.
  private final HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allUnderlay;

  /**
   * Constructor of LocalUnderlay.
   *
   * @param selfAddress Address of the underlay
   * @param port        port of the Underlay
   * @param allUnderlay hashmap of all underlays
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of allUnderlays")
  public LocalUnderlay(String selfAddress, int port, HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allUnderlay) {
    this.selfAddress = selfAddress;
    this.port = port;
    this.allUnderlay = allUnderlay;
  }

  @Override
  public void terminate() {
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public String getAddress() {
    return this.selfAddress;
  }

  @Override
  protected boolean initUnderlay(int port) {
    // the underlay to the underlay cluster
    allUnderlay.put(new SimpleEntry<>(this.selfAddress, this.port), this);
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
  public boolean addInstance(String address, int port, LocalUnderlay underlay) {
    allUnderlay.put(new SimpleEntry<>(address, port), underlay);
    return true;
  }
}
