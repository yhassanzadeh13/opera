package metrics;

import node.Identifier;

/**
 * Noop Collector is a no operation metric collector.
 */
public class NoopCollector implements MetricsCollector {
  @Override
  public Histogram histogram() {
    return new NoopHistogram();
  }

  @Override
  public Gauge gauge() {
    return new NoopGauge();
  }

  @Override
  public Counter counter() {
    return new NoopCounter();
  }

  static class NoopHistogram implements Histogram {
    /**
     * Record a new value for the histogram with a specific name and identifier.
     *
     * @param name  name of the metric.
     * @param id    the node id on which the metric will be registered.
     * @param value value by which metric is recorded.
     */
    @Override
    public void observe(String name, Identifier id, double value) {

    }
  }

  static class NoopGauge implements Gauge {
    /**
     * Set the gauge with a specific name and identifier.
     *
     * @param name  name of the metric.
     * @param id    the node id on which the metric will be registered.
     * @param value value by which metric is set.
     */
    @Override
    public void set(String name, Identifier id, double value) {
      // do nothing
    }
  }

  static class NoopCounter implements Counter {


    /**
     * Increment the counter with a specific name and identifier.
     *
     * @param name name of the metric.
     * @param id   the node id on which the metric will be registered.
     */
    @Override
    public void increment(String name, Identifier id) {
      // do nothing
    }

    /**
     * Decrement the counter with a specific name and identifier.
     *
     * @param name name of the metric.
     * @param id   the node id on which the metric will be registered.
     */
    @Override
    public void decrement(String name, Identifier id) {
      // do nothing
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
      // do nothing
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
      // do nothing
    }
  }
}
