package metrics;

import node.Identifier;

/**
 * The CounterCollector interface is a base interface of counter collector to use for metric collector.
 * inc: is called to increment a metric
 * get: getter of collector
 * register: is called to register new Counter
 */
public interface CounterCollector {
  boolean inc(String name, Identifier id, double v);

  boolean inc(String name, Identifier id);

  // TODO: should be replaced by getCounter
  double get(String name, Identifier id);

  void register(String name, String namespace, String subsystem, String helpMessage);
}
