package metrics.integration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import modules.logger.Logger;
import modules.logger.OperaLogger;

/**
 * Creates a metrics collection network that is composed of a grafana and a prometheus containers.
 * The grafana container is exposed at localhost:3000.
 * The prometheus container is exposed at localhost:9090.
 */
public class MetricsNetwork {
  protected static final String NETWORK_NAME = "opera_network";
  // common
  private static final String MAIN_TAG = "main";
  private static final String USER_DIR = "user.dir";
  private static final String NETWORK_DRIVER_NAME = "bridge";
  // Prometheus
  private static final int PROMETHEUS_PORT = 9090;
  private static final String PROMETHEUS_CONTAINER_NAME = "opera_prometheus";
  private static final String PROMETHEUS_IMAGE = "prom/prometheus";
  private static final String PROMETHEUS_VOLUME = "opera_prometheus_volume";
  private static final String PROMETHEUS_MAIN_CMD = "prom/prometheus:main";
  private static final String PROMETHEUS_VOLUME_BINDING_ETC = "/prometheus" + ":" + "/etc/prometheus";
  private static final String PROMETHEUS_VOLUME_BINDING_VOLUME = "prometheus_volume" + ":" + "/prometheus";
  // Grafana
  private static final int GRAFANA_PORT = 3000;
  private static final String GRAFANA_CONTAINER_NAME = "opera_grafana";
  private static final String GRAFANA_VOLUME = "opera_grafana_volume";
  private static final String GRAFANA_IMAGE = "grafana/grafana";
  private static final String GRAFANA_MAIN_CMD = "grafana/grafana:main";
  private static final String GRAFANA_NO_SIGN_UP = "GF_USERS_ALLOW_SIGN_UP=false";
  private static final String GRAFANA_VOLUME_BINDING = "grafana_volume:/var/lib/grafana";
  private static final String GRAFANA_ADMIN_USER_NAME = "GF_SECURITY_ADMIN_USER=${ADMIN_USER" + ":-admin}";
  private static final String GRAFANA_ADMIN_PASSWORD = "GF_SECURITY_ADMIN_PASSWORD=$" + "{ADMIN_PASSWORD:-admin}";
  private static final String GRAFANA_DASHBOARD_BINDING = "/grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards";
  private static final String GRAFANA_DATA_SOURCE_BINDING = "/grafana/provisioning/datasources:/etc/grafana/provisioning/datasources";
  protected final DockerClient dockerClient;
  private final Logger logger = OperaLogger.getLoggerForSimulator(MetricsNetwork.class.getCanonicalName());

  /**
   * Default constructor.
   */
  public MetricsNetwork() {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    DockerHttpClient client = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost())
                                                                  .sslConfig(config.getSSLConfig())
                                                                  .maxConnections(100)
                                                                  .connectionTimeout(Duration.ofSeconds(30))
                                                                  .responseTimeout(Duration.ofSeconds(45))
                                                                  .build();
    this.dockerClient = DockerClientImpl.getInstance(config, client);
  }

  /**
   * Creates and runs a prometheus and grafana containers that are interconnected.
   *
   * @throws IllegalStateException when container creation faces an illegal state.
   */
  public void runMetricsTestNet() throws IllegalStateException {
    // Volume check + create if absent
    logger.info("creating prometheus volume");
    this.createVolumesIfNotExist(PROMETHEUS_VOLUME);
    logger.info("created grafana volume");
    logger.info("creating grafana volume");
    this.createVolumesIfNotExist(GRAFANA_VOLUME);
    logger.info("created grafana volume");

    // Network
    logger.info("creating docker network");
    this.createNetworkIfNotExist();
    logger.info("created docker network");

    // Prometheus
    logger.info("creating prometheus container");
    if (this.isContainerRunning(PROMETHEUS_CONTAINER_NAME)) {
      logger.warn("prometheus container already running, skipping creation");
    } else {
      CreateContainerResponse prometheusContainer = this.getStoppedContainer(PROMETHEUS_CONTAINER_NAME);
      if (prometheusContainer == null) {
        logger.info("prometheus container not found, creating new one, this can take time...");
        prometheusContainer = this.createPrometheusContainer();
      }
      dockerClient.startContainerCmd(prometheusContainer.getId())
                  .exec();
      logger.info("created prometheus container");
    }
    logger.info("created prometheus container");

    // Grafana
    logger.info("creating grafana container");
    if (this.isContainerRunning(GRAFANA_CONTAINER_NAME)) {
      logger.warn("grafana container already running, skipping creation");
    } else {
      CreateContainerResponse grafanaContainer = this.getStoppedContainer(GRAFANA_CONTAINER_NAME);
      if (grafanaContainer == null) {
        logger.info("grafana container not found, creating new one, this can take time...");
        grafanaContainer = this.createGrafanaContainer();
      }
      dockerClient.startContainerCmd(grafanaContainer.getId())
                  .exec();
      logger.info("created grafana container");
    }

    this.logger.info("prometheus is running at localhost:{}",
                     PROMETHEUS_PORT);
    this.logger.info("grafana is running at localhost:{}",
                     GRAFANA_PORT);
  }

  /**
   * Checks for existence of given volume name in the client, and creates one with the
   * given name if volume name does not exist.
   *
   * @param volumeName volume name to create.
   */
  protected void createVolumesIfNotExist(String volumeName) {
    ListVolumesResponse volumesResponse = this.dockerClient.listVolumesCmd()
                                                           .exec();
    List<InspectVolumeResponse> volumes = volumesResponse.getVolumes();

    for (InspectVolumeResponse v : volumes) {
      if (v.getName()
           .equals(volumeName)) {
        // volume exists
        return;
      }
    }

    // volume name does not exist, create one.
    this.dockerClient.createVolumeCmd()
                     .withName(volumeName)
                     .exec();
  }

  /**
   * Checks for existence of the given network in the client, and creates one with the given name
   * if the network does not exist.
   */
  private void createNetworkIfNotExist() {
    List<Network> networks = this.dockerClient.listNetworksCmd()
                                              .exec();

    for (Network n : networks) {
      if (n.getName()
           .equals(NETWORK_NAME)) {
        // network exists
        return;
      }
    }

    // network does not exist, create one/
    dockerClient.createNetworkCmd()
                .withName(NETWORK_NAME)
                .withDriver(NETWORK_DRIVER_NAME)
                .exec();
  }

  /**
   * Creates and returns a Grafana container.
   *
   * @return create container response for grafana.
   * @throws IllegalStateException when container creation faces an illegal state.
   */
  private CreateContainerResponse createGrafanaContainer() throws IllegalStateException {
    // pull image
    try {
      this.dockerClient.pullImageCmd(GRAFANA_IMAGE)
                       .withTag(MAIN_TAG)
                       .exec(new PullImageResultCallback())
                       .awaitCompletion(300, // to account for slow internet connections.
                                        TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      throw new IllegalStateException("(timeout) could not run grafana container" + ex);
    }

    Ports grafanaPortBindings = new Ports();
    grafanaPortBindings.bind(ExposedPort.tcp(GRAFANA_PORT),
                             Ports.Binding.bindPort(GRAFANA_PORT));

    List<Bind> grafBinds = new ArrayList<Bind>();
    grafBinds.add(Bind.parse(GRAFANA_VOLUME_BINDING));
    grafBinds.add(Bind.parse(System.getProperty(USER_DIR) + GRAFANA_DASHBOARD_BINDING));
    grafBinds.add(Bind.parse(System.getProperty(USER_DIR) + GRAFANA_DATA_SOURCE_BINDING));

    HostConfig hostConfig = new HostConfig().withBinds(grafBinds)
                                            .withNetworkMode(NETWORK_NAME)
                                            .withPortBindings(grafanaPortBindings);

    try {
      return this.dockerClient.createContainerCmd(GRAFANA_MAIN_CMD)
                              .withName(GRAFANA_CONTAINER_NAME)
                              .withTty(true)
                              .withEnv(GRAFANA_ADMIN_USER_NAME)
                              .withEnv(GRAFANA_ADMIN_PASSWORD)
                              .withEnv(GRAFANA_NO_SIGN_UP)
                              .withHostConfig(hostConfig)
                              .exec();
    } catch (ConflictException ex) {
      // reaching here means there is a bug in the code.
      throw new IllegalStateException("container already exists (conflict exception)" + ex);
    }
  }

  /**
   * Creates and returns a Prometheus container.
   *
   * @return create container response for prometheus.
   * @throws IllegalStateException when container creation faces an illegal state.
   */
  private CreateContainerResponse createPrometheusContainer() throws IllegalStateException {
    try {
      this.dockerClient.pullImageCmd(PROMETHEUS_IMAGE)
                       .withTag(MAIN_TAG)
                       .exec(new PullImageResultCallback())
                       .awaitCompletion(300, // to account for a slow internet connection.
                                        TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      throw new IllegalStateException("(timeout) could not run prometheus container" + ex);
    }

    Ports promPortBindings = new Ports();
    promPortBindings.bind(ExposedPort.tcp(PROMETHEUS_PORT),
                          Ports.Binding.bindPort(PROMETHEUS_PORT));

    List<Bind> promBinds = new ArrayList<Bind>();
    promBinds.add(Bind.parse(System.getProperty(USER_DIR) + PROMETHEUS_VOLUME_BINDING_ETC));
    promBinds.add(Bind.parse(PROMETHEUS_VOLUME_BINDING_VOLUME));

    HostConfig hostConfig = new HostConfig().withBinds(promBinds)
                                            .withNetworkMode(NETWORK_NAME)
                                            .withPortBindings(promPortBindings);

    try {
      return this.dockerClient.createContainerCmd(PROMETHEUS_MAIN_CMD)
                              .withName(PROMETHEUS_CONTAINER_NAME)
                              .withTty(true)
                              .withHostConfig(hostConfig)
                              .exec();
    } catch (ConflictException ex) {
      // reaching here means there is a bug in the code.
      throw new IllegalStateException("container already exists (conflict exception)" + ex);
    }
  }

  /**
   * Returns a list of containers with the given name. Includes stopped containers.
   *
   * @param name name of container to find.
   * @return list of containers with the given name.
   */
  private List<Container> findContainerWithName(String name) {
    return this.dockerClient.listContainersCmd()
                            .withShowAll(true)
                            .withNameFilter(List.of(name))
                            .exec();
  }

  /**
   * Returns whether the given container is running.
   *
   * @param container container to check.
   * @return true if the container is running, false otherwise.
   */
  private boolean isContainerRunning(Container container) {
    return "running".equalsIgnoreCase(container.getState());
  }

  /**
   * Returns whether a container with the given name is running.
   *
   * @param name name of container to check.
   * @return true if a container with the given name is running, false otherwise.
   */
  // TODO: write test for this.
  private boolean isContainerRunning(String name) {
    List<Container> containers = this.findContainerWithName(name);
    for (Container c : containers) {
      if (this.isContainerRunning(c)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns a stopped container with the given name. Returns null if no such container exists.
   *
   * @param name name of container to find.
   * @return stopped container with the given name.
   */
  private CreateContainerResponse getStoppedContainer(String name) {
    List<Container> containers = this.findContainerWithName(name);
    for (Container c : containers) {
      if (this.isContainerRunning(c)) {
        continue;
      }
      CreateContainerResponse response = new CreateContainerResponse();
      response.setId(c.getId());
      return response;
    }

    return null;
  }
}

