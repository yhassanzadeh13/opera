package metrics;

import node.Identifier;

/**
 * A base interface for counter collector.
 */
public interface Counter {
  /**
   * Increment the counter with a specific name and identifier.
   *
   * @param id the node id on which the metric will be registered.
   */
  void increment(Identifier id);

  /**
   * Increment the counter with a specific name and identifier by a specific value.
   *
   * @param name  name of the metric.
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is incremented.
   */
  void increment(String name, Identifier id, double value);


  /**
   * Decrement the counter with a specific name and identifier.
   *
   * @param name name of the metric.
   * @param id   the node id on which the metric will be registered.
   */
  void decrement(String name, Identifier id);


  /**
   * Decrement the counter with a specific name and identifier by a specific value.
   *
   * @param name  name of the metric.
   * @param id    the node id on which the metric will be registered.
   * @param value value by which metric is decremented.
   */
  void decrement(String name, Identifier id, double value);
}
