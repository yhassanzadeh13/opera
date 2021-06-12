package Metrics;

import io.prometheus.client.Counter;

import java.util.UUID;

public interface CounterCollector {
    boolean inc(String name, UUID id, double v);

    boolean inc(String name, UUID id);

    double get(String name, UUID id);

    Counter getMetric(String name);

    boolean register(String name);
}
