package metrics;

import io.prometheus.client.Histogram;
import java.util.UUID;

/**
 *
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
