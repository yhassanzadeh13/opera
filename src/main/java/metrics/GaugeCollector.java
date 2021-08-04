package metrics;

import io.prometheus.client.Gauge;
import java.util.UUID;

/**
 *
 */
public interface GaugeCollector {
  boolean register(String name);

  boolean inc(String name, UUID id);

  boolean inc(String name, UUID id, double v);

  boolean dec(String name, UUID id, double v);

  boolean dec(String name, UUID id);

  boolean set(String name, UUID id, double v);

  double get(String name, UUID id);

  Gauge getMetric(String name);

}
