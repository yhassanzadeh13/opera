package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.prometheus.client.exporter.HTTPServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * static class for providing various utils related to the simulator.
 */
public class SimulatorUtils {

  public static String hashPairOfNodes(UUID a, UUID b) {
    return a.toString() + b.toString();
  }

  /**
   * Configure the simulator with prometheus and grafana by running the docker provided under dockprom
   * By default, it uses 2000 as a metrics exposer port.
   */
  public static void configurePrometheus() {
    int exposerPort = 2000;
    try {
      String localAddress = InetAddress.getLocalHost().getHostAddress();
      Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
      for (; n.hasMoreElements(); ) {
        NetworkInterface e = n.nextElement();
        Enumeration<InetAddress> a = e.getInetAddresses();
        for (; a.hasMoreElements(); ) {
          InetAddress addr = a.nextElement();
          if (addr.getHostAddress().startsWith("192")) {
            localAddress = addr.getHostAddress();
            break;

          }
        }
      }

      // Change the prometheus configuration
      ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
      File prometheusConfig = new File("./dockprom/prometheus/prometheus.yml");
      Map<String, Object> config = objectMapper.readValue(prometheusConfig, new TypeReference<Map<String, Object>>() {
      });
      List<Object> scrapeConfigs = (List<Object>) config.get("scrape_configs");


      Map<String, Object> simulatorJob = (Map<String, Object>) scrapeConfigs.get(0);
      List<Object> staticConfigs = (List<Object>) simulatorJob.get("static_configs");
      Map<String, Object> targets = (Map<String, Object>) staticConfigs.get(0);
      targets.put("targets", Arrays.asList(localAddress + ":" + exposerPort));

      // write again on the file
      objectMapper.writeValue(new File("./dockprom/prometheus/prometheus.yml"), config);

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
        HTTPServer server = new HTTPServer(exposerPort);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
