package metrics;

import io.prometheus.client.Histogram;
import node.Identifier;

/**
 * The CounterCollector interface is a base interface of counter collector to use for metric collector.
 * observe: Record a new value for the histogram with a specific name and identifier.
 * getMetric: getter of collector
 * startTimer: Starts time for recording duration and adds it to histogram with a specific name, and timer identifier.
 * observeDuration: Observe the duration of the already started timer with a specific name and timer identifier.
 * tryObserveDuration: Silently observes the duration of the already started timer with a specific name and timer identifier.
 * register: is called to register new Histogram
 */
public interface HistogramCollector {
  void observe(String name, Identifier id, double v);

  Histogram get(String name) throws IllegalArgumentException;

  void register(String name, String namespace, String subsystem, String helpMessage, double[] buckets) throws IllegalArgumentException;
}
