package metrics;

import io.prometheus.client.Gauge;
import java.util.UUID;

/**
 * The GaugeCollector interface is a base interface for gauge collector.
 */
public interface GaugeCollector {
  void register(String name, String namespace, String subsystem, String helpMessage);

  boolean set(String name, UUID id, double v);

  double get(String name, UUID id);

  Gauge getMetric(String name);
}
