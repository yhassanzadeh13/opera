package Metrics;

public interface MetricsCollector {
    HistogramCollector Histogram();
    GaugeCollector Gauge();
    CounterCollector Counter();
}
