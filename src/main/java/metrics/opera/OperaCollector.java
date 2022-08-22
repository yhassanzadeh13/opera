package metrics.opera;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import metrics.CounterCollector;
import metrics.GaugeCollector;
import metrics.HistogramCollector;
import metrics.MetricsCollector;

/**
 * Simulator Collector is a prometheus-based metric collector which uses simulator counter, gauge and histogram.
 *
 */
public class OperaCollector implements MetricsCollector {
  private final OperaCounter counter;
  private final OperaGauge gauge;
  private final OperaHistogram histogram;

  /**
   * Simulator Collector initializer.
   */
  public OperaCollector() {
    this.counter = new OperaCounter();
    this.gauge = new OperaGauge();
    this.histogram = new OperaHistogram();
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public HistogramCollector histogram() {
    return this.histogram;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public GaugeCollector gauge() {
    return this.gauge;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public CounterCollector counter() {
    return this.counter;
  }
}
