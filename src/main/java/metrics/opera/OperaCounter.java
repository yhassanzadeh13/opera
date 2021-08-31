package metrics.opera;

import io.prometheus.client.Counter;
import java.util.UUID;
import metrics.CounterCollector;
import simulator.Simulator;


/**
 * This class provides a prometheus-based counter for extracting metrics
 * For every registered metric, the metric will be collected for each node separately
 * Counters can be incremented but never decremented.
 */
public class OperaCounter extends OperaMetric implements CounterCollector {

  /**
   * increment a metric.
   *
   * @param name name of the metric
   * @param id   the node id on which the metric will be register
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

  /**
   * metric label getter from name and id.
   *
   * @param name name of the metric
   * @param id   id to get
   * @return label of the metric
   */
  public double get(String name, UUID id) {
    Counter metric = getMetric(name);
    if (metric == null) {
      return 0;
    }
    return metric.labels(id.toString()).get();
  }

  /**
   * Return prometheus metric for a specific name.
   *
   * @param name name of the metric.
   * @return prometheus metric
   */
  public Counter getMetric(String name) {
    if (!collectors.containsKey(name)) {
      Simulator.getLogger().error("[SimulatorCounter] could not find a metric with name " + name);
      System.err.println("[SimulatorCounter] could not find a metric with name " + name);
      return null;
    }
    if (collectorsTypes.get(name) != Type.COUNTER) {
      Simulator.getLogger().error("[SimulatorCounter] metric registered with the name " + name
            + " is not a counter");
      System.err.println("[SimulatorCounter] metric registered with the name " + name
            + " is not a counter");
      return null;
    }
    return (Counter) collectors.get(name);
  }

  /**
   * Register a new metric with a specific name.
   *
   * @param name name of the metric
   * @return True in case of success
   */
  public boolean register(String name) {
    if (!collectors.containsKey(name)) {
      collectors.put(name,
            Counter.build().namespace(NAMESPACE).name(name).help(HELP_MSG).labelNames(LABEL_NAME).register());
      collectorsTypes.put(name, Type.COUNTER);
    } else {
      if (collectorsTypes.get(name) != Type.COUNTER) {
        System.err.println(
              "[SimulatorCounter] Collector name is already registered with a different type "
              + collectorsTypes.get(name));
        Simulator.getLogger().error(
              "[SimulatorCounter] Collector name is already registered with a different type "
              + collectorsTypes.get(name));
        return false;
      }
    }
    return true;
  }
}
