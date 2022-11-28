package metrics;

import metrics.opera.OperaCounter;
import metrics.opera.OperaGauge;
import metrics.opera.OperaHistogram;

/**
 * Metric collector is a collector which consists of histogram collector, gauge collector and counter collector.
 */
public interface MetricsCollector {
  Histogram histogram();

  Gauge gauge();

  Counter counter();
}
