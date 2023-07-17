package metrics.integration;

import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;

/**
 * HTTP Server constructor class for the Prometheus exposer server.
 */
public class MetricServer {
  private static final int SERVER_PORT = 8081;
  private HTTPServer server;

  /**
   * Initiates the Prometheus Exposer HTTP Server.
   */
  public void start() throws IllegalStateException {
    try {
      server = new HTTPServer(SERVER_PORT);
    } catch (IOException e) {
      throw new IllegalStateException("could not start metrics server:\t" + e);
    }
  }

  /**
   * Terminates the Prometheus Exposer HTTP Server.
   */
  public void terminate() throws IllegalStateException {
    try {
      server.close();
    } catch (Exception e) {
      throw new IllegalStateException("could not stop metrics server:\t" + e);
    }
  }

}
