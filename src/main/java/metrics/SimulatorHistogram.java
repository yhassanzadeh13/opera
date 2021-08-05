package metrics;

import io.prometheus.client.Histogram;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.UUID;
import simulator.Simulator;

/**
 * This class provides a prometheus-based histogram for extracting metrics.
 * For every registered metric, the metric will be collected for each node separately
 */

public class SimulatorHistogram extends SimulatorMetric implements HistogramCollector {

  private static final HashMap<String, HashMap<String, ArrayDeque<Histogram.Timer>>> timersByName = new HashMap<>();

  /**
   * Record a new value for the histogram with a specific name and id.
   *
   * @param name name of the metric.
   * @param id id of the node.
   * @param v value to record.
   * @return false if metric is null, true else.
   */
  @Override
  public boolean observe(String name, UUID id, double v) {
    Histogram metric = getMetric(name);
    if (metric == null) {
      return false;
    }
    metric.labels(id.toString()).observe(v);
    return true;
  }

  @Override
  public Histogram getMetric(String name) {
    if (!collectors.containsKey(name)) {
      Simulator.getLogger().fatal("could not find a metric with name " + name);
      return null;
    }
    if (collectorsTypes.get(name) != Type.HISTOGRAM) {
      Simulator.getLogger().fatal(
            "metric registered with the name " + name + " is not a histogram");
      return null;
    }
    return (Histogram) collectors.get(name);
  }

  /**
   * Starts time for recording duration and adds it to histogram with a specific name, and timer ID.
   * The time recording finishes when observeDuration is called with the same name, and timer ID.
   * The time elapsed will be recorded under the node with the given id.
   * If two timers start with the same timer ID, the observeDuration will stop the earliest one.
   *
   * @param name    name of the metric.
   * @param id id of the node.
   * @param timerId id of the timer.
   * @return false if metric is null else return true
   */
  @Override
  public boolean startTimer(String name, UUID id, String timerId) {
    Histogram metric = getMetric(name);
    if (metric == null) {
      return false;
    }

    if (!timersByName.containsKey(name)) {
      timersByName.put(name, new HashMap<>());
    }

    if (!timersByName.get(name).containsKey(timerId)) {
      timersByName.get(name).put(timerId, new ArrayDeque<>());
    }
    try {
      timersByName.get(name).get(timerId).addLast(metric.labels(id.toString()).startTimer());
    } catch (NullPointerException e) {
      Simulator.log.error("[SimulatorHistogram] NullPointerException " + e.getMessage());
    }
    Simulator.getLogger().debug(
          "[SimulatorHistogram] Timer of name " + name
                + " and id " + timerId
                + " has started at time " + System.currentTimeMillis());
    return true;
  }

  /**
   * Observe the duration of the already started timer with a specific name and timer ID.
   * To start a timer, please use the startTimer method.
   *
   * @param name name of the timer
   * @param timerId id of the timer
   * @return false if there is an exception else return true.
   */
  @Override
  public boolean observeDuration(String name, String timerId) {
    try {
      timersByName.get(name).get(timerId).getFirst().observeDuration();
      timersByName.get(name).get(timerId).removeFirst();
      Simulator.getLogger().debug(
            "[SimulatorHistogram] duration of name " + name
                  + " and id " + timerId
                  + " was observed at time " + System.currentTimeMillis());
      return true;
    } catch (Exception e) {
      Simulator.getLogger().error(
            "[SimulatorHistogram] timer with name " + name
                  + " and ID " + timerId + " is not initialized");
      return false;
    }
  }


  /**
   * Silently observes the duration of the already started timer with a specific name and timer ID.
   * In case timer has not already been started, it simply returns without logging any error.
   *
   * @param name name of the timer
   * @param timerId id of the timer
   *
   */
  @Override
  public void tryObserveDuration(String name, String timerId) {
    if (timersByName.get(name) == null || timersByName.get(name).get(timerId) == null) {
      return;
    }
    timersByName.get(name).get(timerId).getFirst().observeDuration();
    timersByName.get(name).get(timerId).removeFirst();
  }


  @Override
  public boolean register(String name) {
    return register(
          name,
          new double[]{0.005, 0.01, 0.05, 0.1, 0.15, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.1});
  }

  /**
   * Register a new histogram with given buckets.
   *
   * @param name name for the histogram.
   * @param buckets list of buckets
   * @return false if type of the name is different than histogram type, else return true.
   */
  @Override
  public boolean register(String name, double[] buckets) {
    if (!collectors.containsKey(name)) {
      collectors.put(
            name,
            Histogram.build().buckets(buckets)
                  .namespace(NAMESPACE).name(name).help(HELP_MSG)
                  .labelNames(LABEL_NAME).register());
      collectorsTypes.put(name, Type.HISTOGRAM);
      Simulator.getLogger().info(
            "[SimulatorHistogram] Collector with name " + name + " was registered");
    } else {
      // TODO: should throw an exception.
      if (collectorsTypes.get(name) != Type.HISTOGRAM) {
        System.err.println(
              "[SimulatorHistogram] Collector name is already registered with a different type "
                    + collectorsTypes.get(name));
        Simulator.getLogger().error(
              "[SimulatorHistogram] Collector name is already registered with a different type "
                    + collectorsTypes.get(name));
        return false;
      }
    }
    return true;
  }


}
