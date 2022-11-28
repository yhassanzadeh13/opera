package metrics.opera;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import metrics.MetricsCollector;

/**
 * Simulator Collector is a prometheus-based metric collector which uses simulator counter, gauge and histogram.
 *
 */
public class OperaMetricsCollector implements MetricsCollector {
  private final OperaCounter counter;
  private final OperaGauge gauge;
  private final OperaHistogram histogram;

  /**
   * Simulator Collector initializer.
   */
  public OperaMetricsCollector() {
    this.counter = new OperaCounter();
    this.gauge = new OperaGauge();
    this.histogram = new OperaHistogram();
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public OperaHistogram histogram() {
    return this.histogram;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public OperaGauge gauge() {
    return this.gauge;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "it is meant to expose internal state")
  public OperaCounter counter() {
    return this.counter;
  }
}
