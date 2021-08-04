package metrics;

/**
 *
 */
public interface MetricsCollector {
  HistogramCollector histogram();

  GaugeCollector gauge();

  CounterCollector counter();
}
