package metrics.opera;

import metrics.Constants;
import metrics.Gauge;
import node.Identifier;

public class OperaGauge implements Gauge {
  private final io.prometheus.client.Gauge gauge;

  /**
   * Registers a gauge.
   *
   * @param name        name of gauge metric.
   * @param namespace   namespace of gauge metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics,
   *                    e.g., network.latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   */
  public OperaGauge(String name, String namespace, String subsystem, String helpMessage) {
    this.gauge = io.prometheus.client.Gauge.build()
        .namespace(namespace).subsystem(subsystem).name(name).help(helpMessage).labelNames(Constants.IDENTIFIER).register();
  }

  /**
   * Set the gauge with a specific name and identifier.
   *
   * @param name  name of the metric.
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is set.
   */
  @Override
  public void set(String name, Identifier id, double value) {
    this.gauge.labels(id.toString()).set(value);
  }
}
