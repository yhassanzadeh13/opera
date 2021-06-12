package Metrics;

public interface MetricsCollector {
    HistogramCollector getHistogramCollector();
    GaugeCollector getGaugeCollector();
    CounterCollector getCounterCollector();
}
