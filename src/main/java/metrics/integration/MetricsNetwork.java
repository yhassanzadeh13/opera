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
  private final Logger logger = OperaLogger.getLoggerForSimulator(MetricsNetwork.class.getCanonicalName());
  protected static final String NETWORK_NAME = "network";
  // common
  private static final String MAIN_TAG = "main";
  private static final String USER_DIR = "user.dir";
  private static final String NETWORK_DRIVER_NAME = "bridge";
  // Prometheus
  private static final int PROMETHEUS_PORT = 9090;
  private static final String PROMETHEUS = "prometheus";
  private static final String PROMETHEUS_IMAGE = "prom/prometheus";
  private static final String PROMETHEUS_VOLUME = "prometheus_volume";
  private static final String PROMETHEUS_MAIN_CMD = "prom/prometheus:main";
  private static final String PROMETHEUS_VOLUME_BINDING_ETC = "/prometheus" + ":" + "/etc/prometheus";
  private static final String PROMETHEUS_VOLUME_BINDING_VOLUME = "prometheus_volume" + ":" + "/prometheus";
  // Grafana
  private static final int GRAFANA_PORT = 3000;
  private static final String GRAFANA = "grafana";
  private static final String GRAFANA_VOLUME = "grafana_volume";
  private static final String GRAFANA_IMAGE = "grafana/grafana";
  private static final String GRAFANA_MAIN_CMD = "grafana/grafana:main";
  private static final String GRAFANA_NO_SIGN_UP = "GF_USERS_ALLOW_SIGN_UP=false";
  private static final String GRAFANA_VOLUME_BINDING = "grafana_volume:/var/lib/grafana";
  private static final String GRAFANA_ADMIN_USER_NAME = "GF_SECURITY_ADMIN_USER=${ADMIN_USER:-admin}";
  private static final String GRAFANA_ADMIN_PASSWORD = "GF_SECURITY_ADMIN_PASSWORD=${ADMIN_PASSWORD:-admin}";
  private static final String GRAFANA_DASHBOARD_BINDING =
      "/grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards";
  private static final String GRAFANA_DATA_SOURCE_BINDING =
      "/grafana/provisioning/datasources:/etc/grafana/provisioning/datasources";
  protected final DockerClient dockerClient;

  /**
   * Default constructor.
   */
  public MetricsNetwork() {
    DockerClientConfig config = DefaultDockerClientConfig
        .createDefaultConfigBuilder()
        .build();

    DockerHttpClient client = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
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
    this.createVolumesIfNotExist(PROMETHEUS_VOLUME);
    this.createVolumesIfNotExist(GRAFANA_VOLUME);

    // Network
    this.createNetworkIfNotExist();

    // Prometheus
    try {
      CreateContainerResponse prometheusContainer = createPrometheusContainer();
      dockerClient
          .startContainerCmd(prometheusContainer.getId())
          .exec();
    } catch (ContainerAlreadyExistsException e) {
      logger.warn("prometheus container already exists, skipping creation");
    }


    // Grafana
    try {
      CreateContainerResponse grafanaContainer = this.createGrafanaContainer();
      dockerClient
          .startContainerCmd(grafanaContainer.getId())
          .exec();
    } catch (ContainerAlreadyExistsException e) {
      logger.warn("grafana container already exists, skipping creation");
    }

    this.logger.info("prometheus is running at localhost:{}", PROMETHEUS_PORT);
    this.logger.info("grafana is running at localhost:{}", GRAFANA_PORT);
  }

  /**
   * Checks for existence of given volume name in the client, and creates one with the
   * given name if volume name does not exist.
   *
   * @param volumeName volume name to create.
   */
  protected void createVolumesIfNotExist(String volumeName) {
    ListVolumesResponse volumesResponse = this.dockerClient.listVolumesCmd().exec();
    List<InspectVolumeResponse> volumes = volumesResponse.getVolumes();

    for (InspectVolumeResponse v : volumes) {
      if (v.getName().equals(volumeName)) {
        // volume exists
        return;
      }
    }

    // volume name does not exist, create one.
    this.dockerClient.createVolumeCmd().withName(volumeName).exec();
  }

  /**
   * Checks for existence of the given network in the client, and creates one with the given name
   * if the network does not exist.
   */
  private void createNetworkIfNotExist() {
    List<Network> networks = this.dockerClient.listNetworksCmd().exec();

    for (Network n : networks) {
      if (n.getName().equals(NETWORK_NAME)) {
        // network exists
        return;
      }
    }

    // network does not exist, create one/
    dockerClient.createNetworkCmd().withName(NETWORK_NAME).withDriver(NETWORK_DRIVER_NAME).exec();
  }

  /**
   * Creates and returns a Grafana container.
   *
   * @return create container response for grafana.
   * @throws IllegalStateException when container creation faces an illegal state.
   */
  private CreateContainerResponse createGrafanaContainer() throws IllegalStateException, ContainerAlreadyExistsException {
    try {
      this.dockerClient.pullImageCmd(GRAFANA_IMAGE)
          .withTag(MAIN_TAG)
          .exec(new PullImageResultCallback())
          .awaitCompletion(60, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      throw new IllegalStateException("could not run grafana container" + ex);
    }

    Ports grafanaPortBindings = new Ports();
    grafanaPortBindings.bind(ExposedPort.tcp(GRAFANA_PORT), Ports.Binding.bindPort(GRAFANA_PORT));

    List<Bind> grafBinds = new ArrayList<Bind>();
    grafBinds.add(Bind.parse(GRAFANA_VOLUME_BINDING));
    grafBinds.add(Bind.parse(System.getProperty(USER_DIR) + GRAFANA_DASHBOARD_BINDING));
    grafBinds.add(Bind.parse(System.getProperty(USER_DIR) + GRAFANA_DATA_SOURCE_BINDING));

    HostConfig hostConfig = new HostConfig()
        .withBinds(grafBinds)
        .withNetworkMode(NETWORK_NAME)
        .withPortBindings(grafanaPortBindings);

    try {
      return this.dockerClient
          .createContainerCmd(GRAFANA_MAIN_CMD)
          .withName(GRAFANA)
          .withTty(true)
          .withEnv(GRAFANA_ADMIN_USER_NAME)
          .withEnv(GRAFANA_ADMIN_PASSWORD)
          .withEnv(GRAFANA_NO_SIGN_UP)
          .withHostConfig(hostConfig)
          .exec();
    } catch (ConflictException ex) {
      throw new IllegalStateException("grafana container already exists: " + ex);
    }
  }

  /**
   * Creates and returns a Prometheus container.
   *
   * @return create container response for prometheus.
   * @throws IllegalStateException when container creation faces an illegal state.
   */
  private CreateContainerResponse createPrometheusContainer() throws IllegalStateException, ContainerAlreadyExistsException {
    try {
      this.dockerClient.pullImageCmd(PROMETHEUS_IMAGE)
          .withTag(MAIN_TAG)
          .exec(new PullImageResultCallback())
          .awaitCompletion(60, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      throw new IllegalStateException("could not run prometheus container" + ex);
    }

    Ports promPortBindings = new Ports();
    promPortBindings.bind(ExposedPort.tcp(PROMETHEUS_PORT), Ports.Binding.bindPort(PROMETHEUS_PORT));

    List<Bind> promBinds = new ArrayList<Bind>();
    promBinds.add(Bind.parse(System.getProperty(USER_DIR) + PROMETHEUS_VOLUME_BINDING_ETC));
    promBinds.add(Bind.parse(PROMETHEUS_VOLUME_BINDING_VOLUME));

    HostConfig hostConfig = new HostConfig()
        .withBinds(promBinds)
        .withNetworkMode(NETWORK_NAME)
        .withPortBindings(promPortBindings);

    CreateContainerResponse container;
    try {
      container  = this.dockerClient
          .createContainerCmd(PROMETHEUS_MAIN_CMD)
          .withName(PROMETHEUS)
          .withTty(true)
          .withHostConfig(hostConfig)
          .exec();
    } catch (ConflictException ex) {
      throw new ContainerAlreadyExistsException("prometheus container already exists: " + ex);
    }

    return container;
  }

}

