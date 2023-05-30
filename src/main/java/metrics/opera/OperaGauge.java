package metrics.opera;

import metrics.Gauge;
import node.Identifier;

/**
 * Implements the Gauge collector for Opera.
 */
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
   * @param labelNames  label names for this metric.
   */
  public OperaGauge(String name, String namespace, String subsystem, String helpMessage, String... labelNames) {
    this.gauge = io.prometheus.client.Gauge.build().namespace(namespace).subsystem(subsystem).name(name).help(
            helpMessage).labelNames(labelNames).register();
  }

  /**
   * Set the gauge with a specific name and identifier.
   *
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is set.
   */
  @Override
  public void set(Identifier id, double value) {
    this.gauge.labels(id.toString()).set(value);
  }

  /**
   * Set the gauge with a specific name and identifier.
   *
   * @param value value by which metric is set.
   */
  @Override
  public void set(double value) {
    this.gauge.set(value);
  }
}
