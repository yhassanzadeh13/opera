package underlay;

/**
 * consists of type of the underlays.
 */
public enum UnderlayType {
  TCP_PROTOCOL("tcp"),
  UDP_PROTOCOL("udp"),
  JAVA_RMI("underlay.javarmi"),
  MOCK_NETWORK("mockNetwork");

  public final String label;

  UnderlayType(String label) {
    this.label = label;
  }
}
