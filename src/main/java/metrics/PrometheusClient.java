package metrics;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.exporter.HTTPServer;

/**
 * Encapsulates connecting to the prometheus server for sake of metrics collection.
 */
public class PrometheusClient {
  /**
   * Collected metrics are exposed on this port for prometheus server to read.
   * To verify metrics are collected correctly by Opera, check "localhost:2000/metric".
   */
  private static final int EXPOSED_PORT = 9000;

  /**
   * Command to run the docker compose file for prometheus and grafana.
   */
  private static final String DOCKER_COMPOSE_UP = "docker-compose up -d";

  /**
   * Directory where docker file for prometheus and grafana exist.
   */
  private static final String DOCKER_DIRECTORY = "./dockerprom";

  /**
   * Startup timeout for prometheus and grafana containers.
   */
  private static final int STARTUP_TIMEOUT = 5;

  /**
   * Starts up metrics containers for metrics collection (prometheus), and metrics illustration (grafana).
   *
   * @throws IllegalStateException facing any checked exception while running metrics containers.
   */
  public static void start() throws IllegalStateException {
    try {
      // TODO: this can be further simplified with docker cli for java.
      ProcessBuilder builder = new ProcessBuilder();
      builder.directory(new File(DOCKER_DIRECTORY));
      builder.command("sh", "-c", DOCKER_COMPOSE_UP);
      Process proc = builder.start();

      // waits till docker compose run terminates gracefully,
      // hence signaling containers are running.
      proc.waitFor(STARTUP_TIMEOUT, TimeUnit.SECONDS);
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("could not start metrics containers", e);
    }


    try {
      // exposes metrics client for prometheus server
      HTTPServer server = new HTTPServer(EXPOSED_PORT);
    } catch (IOException e) {
      throw new IllegalStateException("could not startup prometheus http client", e);
    }
  }
}
