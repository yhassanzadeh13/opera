package network.local;

import java.net.InetSocketAddress;

import network.Underlay;
import network.exception.OperaNetworkingException;
import network.model.Message;


/**
 * Local connection underlay implementation.
 */
public class LocalUnderlay extends Underlay {
  /**
   * Address of the current instance of underlay.
   */
  private InetSocketAddress selfAddress;
  private final LocalHub hub;

  /**
   * Constructs a `LocalUnderlay` instance and binds it to the given port.
   *
   * @param selfAddress address of the current instance of underlay.
   */
  public LocalUnderlay(final InetSocketAddress selfAddress, LocalHub hub) {
    this.selfAddress = selfAddress;
    this.hub = hub;
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
    this.hub.registerUnderlay(this.selfAddress, this);
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
    this.hub.routeMessage(targetAddress, message);
  }
}
