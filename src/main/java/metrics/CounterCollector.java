package metrics;

import java.util.UUID;

/**
 * The CounterCollector interface is a base interface of counter collector to use for metric collector.
 * inc: is called to increment a metric
 * get: getter of collector
 * register: is called to register new Counter
 */
public interface CounterCollector {
  boolean inc(String name, UUID id, double v);

  boolean inc(String name, UUID id);

  double get(String name, UUID id);

  boolean register(String name);
}
