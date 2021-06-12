package Metrics;

import io.prometheus.client.Histogram;

import java.util.UUID;

public interface HistogramCollector {
    boolean observe(String name, UUID id, double v);
    Histogram getMetric(String name);
    boolean startTimer(String name, UUID id, String timerID);
    boolean observeDuration(String name, String timerID);
    boolean register(String name);
    boolean register(String name, double[] buckets);
}
