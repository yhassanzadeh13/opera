package metrics.opera;

import io.prometheus.client.Histogram;
import metrics.HistogramCollector;
import simulator.Simulator;


import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.UUID;

/**
 * OperaHistogram provides a prometheus-based histogram collector.
 */
public class OperaHistogram extends OperaMetric implements HistogramCollector {

  private static final HashMap<String, HashMap<String, ArrayDeque<Histogram.Timer>>> timersByName = new HashMap<>();

  /**
   * Record a new value for the histogram with a specific name and id
   *
   * @param name
   * @param id
   * @param v
   * @return
   */
  @Override
  public void observe(String name, UUID id, double v) throws IllegalArgumentException {
    Histogram metric = get(name);
    metric.labels(id.toString()).observe(v);
  }

  @Override
  public Histogram get(String name) throws IllegalArgumentException {
    if (!collectors.containsKey(name) || collectorsTypes.get(name) != Type.HISTOGRAM) {
      throw new IllegalArgumentException("Histogram name does not exist: " + name);
    }
    return (Histogram) collectors.get(name);
  }

  /**
   * Start a time for recoding a duration and add it to the histogram with a specific name, and timer ID. The time recording finishes once
   * the observeDuration method is called with the same name, and timer ID. The time elapsed will be recorded under the node with the given id.
   * In case of starting two timers with the same timer ID, the observeDuration method will stop the earliest one.
   *
   * @param name
   * @param id
   * @param timerID
   * @return
   */
  @Override
  public boolean startTimer(String name, UUID id, String timerID) {
    Histogram metric = get(name);

    if (!timersByName.containsKey(name))
      timersByName.put(name, new HashMap<>());

    if (!timersByName.get(name).containsKey(timerID)) {
      timersByName.get(name).put(timerID, new ArrayDeque<>());
    }
    try {
      timersByName.get(name).get(timerID).addLast(metric.labels(id.toString()).startTimer());
    } catch (NullPointerException e) {
      Simulator.log.error("[SimulatorHistogram] NullPointerException " + e.getMessage());
    }
    Simulator.getLogger().debug("[SimulatorHistogram] Timer of name " + name + " and id " + timerID + " has started at time " + System.currentTimeMillis());
    return true;
  }

  /**
   * Observe the duration of the already started timer with a specific name and timer ID. to start a timer, please use the startTimer method.
   *
   * @param name
   * @param timerID
   * @return
   */
  @Override
  public boolean observeDuration(String name, String timerID) {
    try {
      timersByName.get(name).get(timerID).getFirst().observeDuration();
      timersByName.get(name).get(timerID).removeFirst();
      Simulator.getLogger().debug("[SimulatorHistogram] duration of name " + name + " and id " + timerID + " was observed at time " + System.currentTimeMillis());
      return true;
    } catch (Exception e) {
      Simulator.getLogger().error("[SimulatorHistogram] timer with name " + name + " and ID " + timerID + " is not initialized");
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

  /**
   * Registers a histogram collector. This method is expected to be executed by several instances of nodes assuming a decentralized
   * metrics registration. However, only the first invocation gets through and registers the metric. The rest will be gracefully returned.
   * Since the collector is handled globally in a centralized manner behind the scene, only one successful registration is enough.
   *
   * @param name        name of histogram metric.
   * @param namespace   namespace of histogram metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics, e.g., latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   * @param buckets     histogram buckets. The buckets are cumulative, i.e., each bucket has a length label that accumulates all
   *                    metrics with a value of less than or equal to length.
   *                    For example, if a bucket has length=0.5 then it means that how many samples have less than or equal to 0.5 value.
   * @throws IllegalArgumentException when a different metric type (e.g., counter) with the same name has already been registered.
   */
  @Override
  public void register(String name, String namespace, String subsystem, String helpMessage, double[] buckets) throws IllegalArgumentException {
    if (collectors.containsKey(name)) {
      if (collectorsTypes.get(name) != Type.HISTOGRAM) {
        throw new IllegalArgumentException("Metrics name already taken with another type: " + name + " type: " + collectorsTypes.get(name));
      }
      // collector already registered
      return;
    }
    collectors.put(name, Histogram.build().
        buckets(buckets).
        namespace(namespace).
        name(name).
        help(helpMessage).
        labelNames(LABEL_UUID).
        register());
    collectorsTypes.put(name, Type.HISTOGRAM);
  }
}
