package network;

/**
 * A singleton class which collects metrics for the network layer.
 * It is shared among all the network layers of the nodes in simulation.
 */
public class OperaMiddlewareCollector {
  /**
   * The singleton instance of the collector.
   */
  private static final MiddlewareCollector instance = new MiddlewareCollector();

  /**
   * Private constructor to prevent instantiation.
   *
   * @return the singleton instance of the middleware collector
   */
  public static MiddlewareCollector getInstance() {
    return instance;
  }
}
