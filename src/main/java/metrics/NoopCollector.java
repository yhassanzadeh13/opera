package metrics;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import java.util.UUID;

/**
 *
 */
public class NoopCollector implements MetricsCollector {
  class NoopHistogram implements HistogramCollector {

    @Override
    public boolean observe(String name, UUID id, double v) {
      return false;
    }

    @Override
    public Histogram getMetric(String name) {
      return null;
    }

    @Override
    public boolean startTimer(String name, UUID id, String timerId) {
      return false;
    }

    @Override
    public boolean observeDuration(String name, String timerId) {
      return false;
    }

    @Override
    public void tryObserveDuration(String name, String timerId) {

    }

    @Override
    public boolean register(String name) {
      return false;
    }

    @Override
    public boolean register(String name, double[] buckets) {
      return false;
    }
  }

  class NoopGauge implements GaugeCollector {

    @Override
    public boolean register(String name) {
      return false;
    }

    @Override
    public boolean inc(String name, UUID id) {
      return false;
    }

    @Override
    public boolean inc(String name, UUID id, double v) {
      return false;
    }

    @Override
    public boolean dec(String name, UUID id, double v) {
      return false;
    }

    @Override
    public boolean dec(String name, UUID id) {
      return false;
    }

    @Override
    public boolean set(String name, UUID id, double v) {
      return false;
    }

    @Override
    public double get(String name, UUID id) {
      return 0;
    }

    @Override
    public Gauge getMetric(String name) {
      return null;
    }
  }

  class NoopCounter implements CounterCollector {

    @Override
    public boolean inc(String name, UUID id, double v) {
      return false;
    }

    @Override
    public boolean inc(String name, UUID id) {
      return false;
    }

    @Override
    public double get(String name, UUID id) {
      return 0;
    }

    @Override
    public boolean register(String name) {
      return false;
    }
  }

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
}
