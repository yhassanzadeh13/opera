package metrics.opera;

import java.util.UUID;
import io.prometheus.client.Counter;
import metrics.Constants;
import metrics.CounterCollector;
import simulator.Simulator;

/**
 * This class provides a prometheus-based counter for extracting metrics
 * For every registered metric, the metric will be collected for each node separately
 * Counters can be incremented but never decremented.
 */
public class OperaCounter extends OperaMetric implements CounterCollector {

  /**
   * increment a metric
   *
   * @param name name of the metric
   * @param id   the node id on which the metric will be regi
   * @param v    value
   * @return True in case of success
   */
  public synchronized boolean inc(String name, UUID id, double v) {
    Counter metric = getMetric(name);
    if (metric == null) {
      return false;
    }
    metric.labels(id.toString()).inc(v);
    return true;
  }

  public boolean inc(String name, UUID id) {
    return inc(name, id, 1.0);
  }

  public double get(String name, UUID id) {
    Counter metric = getMetric(name);
    if (metric == null) return 0;
    return metric.labels(id.toString()).get();
  }

  /**
   * Return prometheus metric for a specific name
   *
   * @param name
   * @return
   */
  public Counter getMetric(String name) {
    if (!collectors.containsKey(name)) {
      Simulator.getLogger().error("[SimulatorCounter] could not find a metric with name " + name);
      System.err.println("[SimulatorCounter] could not find a metric with name " + name);
      return null;
    }
    if (collectorsTypes.get(name) != TYPE.COUNTER) {
      Simulator.getLogger().error("[SimulatorCounter] metric registered with the name " + name + " is not a counter");
      System.err.println("[SimulatorCounter] metric registered with the name " + name + " is not a counter");
      return null;
    }
    return (Counter) collectors.get(name);
  }

  /**
   * Registers a counter collector.
   * This method is expected to be executed by several instances of nodes assuming a decentralized metrics registration.
   * However, only the first invocation gets through and registers the metric. The rest will be idempotent.
   * Since the collector is handled globally in a centralized manner behind the scene, only one successful
   * registration is enough.
   *
   * @param name        name of counter metric.
   * @param namespace   namespace of counter metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics,
   *                    e.g., latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   * @throws IllegalArgumentException when a different metric type (e.g., histogram) with the
   *                                  same name has already been registered.
   */
  public void register(String name, String namespace, String subsystem, String helpMessage)
      throws IllegalArgumentException {
    if (!collectors.containsKey(name)) {
      if (collectorsTypes.get(name) != TYPE.COUNTER) {
        throw new IllegalArgumentException("metrics name already taken with another type: "
            + name + " type: " + collectorsTypes.get(name));
      }
      // collector already registered
      return;
    }
    collectors.put(name, Counter.build()
        .namespace(namespace)
        .subsystem(subsystem)
        .name(name)
        .help(helpMessage)
        .labelNames(Constants.UUID)
        .register());
    collectorsTypes.put(name, TYPE.COUNTER);
  }
}