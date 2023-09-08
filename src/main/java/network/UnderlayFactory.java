package network;

import java.net.InetSocketAddress;

import network.javarmi.JavaRmiUnderlay;
import network.local.LocalHub;
import network.local.LocalUnderlay;
import network.tcp.TcpUnderlay;
import network.udp.UdpUnderlay;

/**
 * UnderlayFactory is a factory which consists of Underlays.
 */
public class UnderlayFactory {
  private UnderlayFactory() {
  }

  private static final LocalHub localHub = new LocalHub();

  /**
   * Creates and returns a new LocalUnderlay instance.
   *
   * @param address the address of the underlay.
   * @param network the network layer of the underlay.
   * @return a new LocalUnderlay instance.
   */
  public static LocalUnderlay createMockUnderlay(InetSocketAddress address, Network network) {
    LocalUnderlay underlay = new LocalUnderlay(address, localHub);
    underlay.initialize(address.getPort(), network);
    return underlay;
  }

  /**
   * get a new underlay instance.
   *
   * @param underlayName the underlay type name according to underlayTypes yaml file
   * @param port         port of the underlay
   * @param network      middle layer of the underlay
   * @return new underlay instance according to the given type
   */
  public static Underlay newUnderlay(NetworkProtocol underlayName, int port, Network network) {
    Underlay underlay;
    switch (underlayName) {
      case JAVA_RMI:
        underlay = new JavaRmiUnderlay();
        break;

      case TCP_PROTOCOL:
        underlay = new TcpUnderlay();
        break;

      case UDP_PROTOCOL:
        underlay = new UdpUnderlay();
        break;

      default:
        throw new IllegalArgumentException("wrong argument name: " + underlayName);
    }

    underlay.initialize(port, network);

    return underlay;
  }
}
