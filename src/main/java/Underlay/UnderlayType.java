package Underlay;

public enum UnderlayType {
    TCP_PROTOCOL("tcp"),
    UDP_PROTOCOL("udp"),
    JAVA_RMI("javaRMI"),
    MOCK_NETWORK("mockNetwork");

    public final String label;

    UnderlayType(String label) {
        this.label = label;
    }
}
