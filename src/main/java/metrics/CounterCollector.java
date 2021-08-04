package metrics;

import java.util.UUID;

/**
 *
 */
public interface CounterCollector {
  boolean inc(String name, UUID id, double v);

  boolean inc(String name, UUID id);

  double get(String name, UUID id);

  boolean register(String name);
}
