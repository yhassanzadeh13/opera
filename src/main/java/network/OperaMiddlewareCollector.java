package network;

/**
 * A singleton class which collects metrics for the network layer.
 * It is shared among all the network layers of the nodes in simulation.
 */
public class OperaMiddlewareCollector {
    /**
     * The singleton instance of the collector.
     */
    private static final NetworkCollector instance = new NetworkCollector();

    /**
     * Private constructor to prevent instantiation.
     *
     * @return the singleton instance of the middleware collector
     */
    public static NetworkCollector getInstance() {
        return instance;
    }
}
