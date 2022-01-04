package network;

/**
 * Available list of networking protocols.
 */
public enum NetworkProtocol {
  TCP_PROTOCOL("tcp"),
  UDP_PROTOCOL("udp"),
  JAVA_RMI("javarmi"),
  MOCK_NETWORK("mockNetwork");

  public final String label;

  NetworkProtocol(String label) {
    this.label = label;
  }
}
