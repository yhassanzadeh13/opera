package metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Encapsulates connecting to the prometheus server for sake of metrics collection.
 */
public class PrometheusClient {
  /**
   * Collected metrics are exposed on this port for prometheus server to read.
   * To verify metrics are collected correctly by Opera, check "localhost:2000/metric".
   */
  private static final int EXPOSED_PORT = 2000;

  /**
   * Configure the simulator with prometheus and grafana by running the docker provided under dockprom
   * By default, it uses 2000 as a metrics exposer port.
   */
  public static void start() {
    try {
      // run the docker
      String cmd = "docker-compose up -d";
      ProcessBuilder builder = new ProcessBuilder();
      builder.directory(new File("./dockprom"));
      builder.command("sh", "-c", cmd);
      Process proc = builder.start();
      proc.waitFor();

      BufferedReader buffer = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      String steam;
      while ((steam = buffer.readLine()) != null) {
        System.out.println(steam);
      }

      // prepare the prometheus connection
      try {
        // initialize prometheus HTTP server
        HTTPServer server = new HTTPServer(EXPOSED_PORT);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
