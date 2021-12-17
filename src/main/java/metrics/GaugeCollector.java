package metrics;

import java.util.UUID;

import io.prometheus.client.Gauge;

/**
 * A base interface for gauge collector.
 */
public interface GaugeCollector {
  void register(String name, String namespace, String subsystem, String helpMessage);

  boolean set(String name, UUID id, double v);

  double get(String name, UUID id);

  Gauge getMetric(String name);
}
