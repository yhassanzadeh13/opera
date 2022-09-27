package metrics;

import java.util.UUID;

import io.prometheus.client.Gauge;
import node.Identifier;

/**
 * A base interface for gauge collector.
 */
public interface GaugeCollector {
  void register(String name, String namespace, String subsystem, String helpMessage);

  boolean set(String name, Identifier id, double v);

  double get(String name, Identifier id);

  Gauge getMetric(String name);
}
