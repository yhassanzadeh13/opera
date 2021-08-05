package metrics;

/**
 * Metric collector is a collector which consists of histogram collector, gauge collector and counter collector.
 */
public interface MetricsCollector {
  HistogramCollector histogram();

  GaugeCollector gauge();

  CounterCollector counter();
}
