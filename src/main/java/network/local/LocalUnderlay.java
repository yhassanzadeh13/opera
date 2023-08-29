package network.local;

import java.net.InetSocketAddress;
import java.util.HashMap;

import network.Underlay;
import network.exception.OperaNetworkingException;
import network.model.Message;


/**
 * Local connection underlay implementation.
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
   * Constructs a `LocalUnderlay` instance and binds it to the given port.
   *
   * @param selfAddress address of the current instance of underlay.
   * @param allUnderlay hash table of the full addresses of all the nodes in the network.
   */
  public LocalUnderlay(final InetSocketAddress selfAddress, final HashMap<InetSocketAddress, LocalUnderlay> allUnderlay) {
    this.selfAddress = selfAddress;
    this.allUnderlay = new HashMap<>(allUnderlay);
  }

  @Override
  public void terminate() {
  }

  /**
   * Returns the port that the underlay is bound to.
   *
   * @return the port that the underlay is bound to.
   */
  @Override
  public int getPort() {
    return this.selfAddress.getPort();
  }

  /**
   * Returns the address of the current instance of underlay.
   *
   * @return the address of the current instance of underlay.
   */
  @Override
  public String getAddress() {
    return this.selfAddress.getAddress().toString();
  }

  /**
   * Constructs a `LocalUnderlay` instance and binds it to the given port.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff the Java RMI initialization was successful.
   */
  @Override
  protected boolean initUnderlay(final int port) {
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
