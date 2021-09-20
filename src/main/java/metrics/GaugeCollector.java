package metrics;

import io.prometheus.client.Gauge;
import java.util.UUID;

/**
 * The GaugeCollector interface is a base interface of collector to use for metric collector.
 * inc: is called to increment a metric
 * dec: is called to decrement a metric
 * set: setter of a new collector
 * get: getter of collector
 * register: is called to register new Gauge
 */
public interface GaugeCollector {
  void register(String name, String namespace, String subsystem, String helpMessage);

  boolean inc(String name, UUID id);

  boolean inc(String name, UUID id, double v);

  boolean dec(String name, UUID id, double v);

  boolean dec(String name, UUID id);

  boolean set(String name, UUID id, double v);

  double get(String name, UUID id);

  Gauge getMetric(String name);

}
