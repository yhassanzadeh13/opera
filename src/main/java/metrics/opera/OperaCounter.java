package metrics.opera;

import metrics.Constants;
import metrics.Counter;
import node.Identifier;

public class OperaCounter implements Counter {
  private final io.prometheus.client.Counter counter;

  /**
   * Registers a counter collector.
   *
   * @param name        name of counter metric.
   * @param namespace   namespace of counter metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics,
   *                    e.g., network.latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   */
  public OperaCounter(String name, String namespace, String subsystem, String helpMessage) {
    this.counter = io.prometheus.client.Counter.build()
        .namespace(namespace).subsystem(subsystem).name(name).help(helpMessage).labelNames(Constants.IDENTIFIER).register();
  }

  /**
   * Increment the counter with a specific name and identifier.
   *
   * @param name name of the metric.
   * @param id   the node id on which the metric will be registered.
   */
  @Override
  public void increment(String name, Identifier id) {
    this.counter.labels(id.toString()).inc();
  }

  /**
   * Decrement the counter with a specific name and identifier.
   *
   * @param name name of the metric.
   * @param id   the node id on which the metric will be registered.
   */
  @Override
  public void decrement(String name, Identifier id) {
    this.counter.labels(id.toString()).inc(-1);
  }

  /**
   * Increment the counter with a specific name and identifier by a specific value.
   *
   * @param name  name of the metric.
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is incremented.
   */
  @Override
  public void increment(String name, Identifier id, double value) {
    this.counter.labels(id.toString()).inc(value);
  }

  /**
   * Decrement the counter with a specific name and identifier by a specific value.
   *
   * @param name  name of the metric.
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is decremented.
   */
  @Override
  public void decrement(String name, Identifier id, double value) {
    this.counter.labels(id.toString()).inc(-value);
  }
}
