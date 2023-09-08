package network.local;

import java.net.InetSocketAddress;
import java.util.HashMap;

import network.model.Message;

/**
 * LocalHub is a hub for all local underlay instances. A local underlay instance does not have any
 * networking capabilities and is only used for testing purposes. It is used to simulate a network
 * of nodes on a single machine. The LocalHub is used to route messages to the correct local underlay.
 */
public class LocalHub {
  private final HashMap<InetSocketAddress, LocalUnderlay> allUnderlays;

  /**
   * Constructs a new LocalHub instance.
   */
  public LocalHub() {
    this.allUnderlays = new HashMap<>();
  }

  /**
   * Registers a new local underlay instance with the given address at the hub.
   *
   * @param selfAddress The address of the local underlay instance.
   * @param underlay The local underlay instance.
   * @return The local underlay instance.
   */
  public LocalUnderlay registerUnderlay(final InetSocketAddress selfAddress, final LocalUnderlay underlay) {
    if (this.allUnderlays.containsKey(selfAddress)) {
      throw new IllegalStateException("Underlay already registered.");
    }
    this.allUnderlays.put(selfAddress, underlay);
    return underlay;
  }

  /**
   * Routes a message to the given target address.
   *
   * @param targetAddress The address of the target node.
   * @param message The message to route.
   */
  public void routeMessage(final InetSocketAddress targetAddress, final Message message) {
    if (!this.allUnderlays.containsKey(targetAddress)) {
      throw new IllegalStateException("Target underlay not registered.");
    }
    this.allUnderlays.get(targetAddress).dispatchRequest(message);
  }
}
