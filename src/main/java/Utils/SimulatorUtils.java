package Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.prometheus.client.exporter.HTTPServer;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * static class for providing various utils related to the simulator
 */
public class SimulatorUtils {

  public static String hashPairOfNodes(UUID a, UUID b) {
    return a.toString() + b.toString();
  }

  /**
   * Configure the simulator with prometheus and grafana by running the docker provided under dockprom
   * By default, it uses 2000 as a metrics exposer port.
   *
   * @param admin    the user name of the admin
   * @param password the corresponding password
   */
  public static void ConfigurePrometheus(String admin, String password) {
    int EXPOSER_PORT = 2000;
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
      List<Object> scrape_configs = (List<Object>) config.get("scrape_configs");


      Map<String, Object> simulator_job = (Map<String, Object>) scrape_configs.get(0);
      List<Object> static_configs = (List<Object>) simulator_job.get("static_configs");
      Map<String, Object> targets = (Map<String, Object>) static_configs.get(0);
      targets.put("targets", Arrays.asList(localAddress + ":" + EXPOSER_PORT));

      // write again on the file
      objectMapper.writeValue(new File("./dockprom/prometheus/prometheus.yml"), config);

      Scanner scanner = new Scanner(System.in);
      System.out.println("Please enter your password");
      String userPassword = scanner.nextLine();


      // ADMIN_USER=admin ADMIN_PASSWORD=admin docker-compose up -d
      Runtime run = Runtime.getRuntime();
      String cmd = "echo \"" + userPassword + "\" | sudo -S ADMIN_USER=" + admin + " ADMIN_PASSWORD=" + password + " docker-compose up -d";
      // System.out.println(cmd);
      // run the docker

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
        HTTPServer server = new HTTPServer(EXPOSER_PORT);
      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
