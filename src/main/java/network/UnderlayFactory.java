package network;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;

import network.javarmi.JavaRmiUnderlay;
import network.local.LocalUnderlay;
import network.tcp.TcpUnderlay;
import network.udp.UdpUnderlay;

/**
 * UnderlayFactory is a factory which consists of Underlays.
 */
public class UnderlayFactory {
  private UnderlayFactory() {
  }

  /**
   * getter of a mock Underlay.
   *
   * @param address          address of the underlay
   * @param port             port of the underlay
   * @param middleLayer      middle layer of the underlay
   * @param allLocalUnderlay hashmap of all underlays
   * @return underlay
   */
  public static LocalUnderlay getMockUnderlay(String address,
                                              int port,
                                              MiddleLayer middleLayer,
                                              HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay) {
    LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
    underlay.initialize(port, middleLayer);
    return underlay;
  }

  /**
   * get a new underlay instance.
   *
   * @param underlayName the underlay type name according to underlayTypes yaml file
   * @param port         port of the underlay
   * @param middleLayer  middle layer of the underlay
   * @return new underlay instance according to the given type
   */
  public static Underlay newUnderlay(UnderlayType underlayName, int port, MiddleLayer middleLayer) {
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
    try {
      underlay.initialize(port, middleLayer);
    } catch (Exception e) {
      System.err.println("[UnderlayFactory] could not create new underlay instance of type " + underlayName);
      e.printStackTrace();
      return null;
    }

    return underlay;
  }
}