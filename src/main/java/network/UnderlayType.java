package network;

/**
 * consists of type of the underlays.
 */
public enum UnderlayType {
  TCP_PROTOCOL("tcp"),
  UDP_PROTOCOL("udp"),
  JAVA_RMI("javarmi"),
  MOCK_NETWORK("mockNetwork");

  public final String label;

  UnderlayType(String label) {
    this.label = label;
  }
}
