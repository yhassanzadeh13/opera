package metrics;

import io.prometheus.client.Histogram;
import java.util.UUID;

/**
 * The CounterCollector interface is a base interface of counter collector to use for metric collector.
 * observe: Record a new value for the histogram with a specific name and id.
 * getMetric: getter of collector
 * startTimer: Starts time for recording duration and adds it to histogram with a specific name, and timer ID.
 * observeDuration: Observe the duration of the already started timer with a specific name and timer ID.
 * tryObserveDuration: Silently observes the duration of the already started timer with a specific name and timer ID.
 * register: is called to register new Histogram
 */
public interface HistogramCollector {
  boolean observe(String name, UUID id, double v);

  Histogram getMetric(String name);

  boolean startTimer(String name, UUID id, String timerId);

  boolean observeDuration(String name, String timerId);

  void tryObserveDuration(String name, String timerId);

  boolean register(String name);

  boolean register(String name, double[] buckets);
}
