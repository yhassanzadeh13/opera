package metrics;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import node.Identifier;

/**
 * Noop Collector is a no operation metric collector.
 */
public class NoopCollector implements MetricsCollector {
  @Override
  public HistogramCollector histogram() {
    return new NoopHistogram();
  }

  @Override
  public GaugeCollector gauge() {
    return new NoopGauge();
  }

  @Override
  public CounterCollector counter() {
    return new NoopCounter();
  }

  static class NoopHistogram implements HistogramCollector {

    @Override
    public void observe(String name, Identifier id, double v) {
    }

    @Override
    public Histogram get(String name) {
      return null;
    }

    @Override
    public void register(String name, String namespace, String subsystem, String helpMessage, double[] buckets)
        throws IllegalArgumentException {
    }
  }

  static class NoopGauge implements GaugeCollector {

    @Override
    public void register(String name, String namespace, String subsystem, String helpMessage) {

    }

    @Override
    public boolean set(String name, Identifier id, double v) {
      return false;
    }

    @Override
    public double get(String name, Identifier id) {
      return 0;
    }

    @Override
    public Gauge getMetric(String name) {
      return null;
    }
  }

  static class NoopCounter implements CounterCollector {

    @Override
    public boolean inc(String name, Identifier id, double v) {
      return false;
    }

    @Override
    public boolean inc(String name, Identifier id) {
      return false;
    }

    @Override
    public double get(String name, Identifier id) {
      return 0;
    }

    @Override
    public void register(String name, String namespace, String subsystem, String helpMessage) {

    }

  }
}
