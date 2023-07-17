package metrics.opera;

import metrics.Histogram;
import node.Identifier;

/**
 * Implements the histogram metric collector for Opera.
 */
public class OperaHistogram implements Histogram {
  private final io.prometheus.client.Histogram histogram;

  /**
   * Constructor for HistogramMetric.
   *
   * @param name        name of histogram metric.
   * @param namespace   namespace of histogram metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics,
   *                    e.g., network.latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   * @param buckets     histogram buckets. The buckets are cumulative,
   *                    i.e., each bucket has a length label that accumulates all
   *                    metrics with a value of less than or equal to length.
   *                    For example, if a bucket has length=0.5 then it means that how many samples have less than or
   *                    equal to 0.5 value.
   * @param labelNames  label names for this metric.
   */
  public OperaHistogram(String name, String namespace, String subsystem, String helpMessage, double[] buckets, String... labelNames) {
    this.histogram = io.prometheus.client.Histogram.build()
      .buckets(buckets)
      .namespace(namespace)
      .name(name)
      .subsystem(subsystem)
      .help(helpMessage)
      .labelNames(labelNames)
      .register();
  }

  /**
   * Constructor for HistogramMetric.
   *
   * @param name        name of histogram metric.
   * @param namespace   namespace of histogram metric, normally refers to a distinct class of opera, e.g., middleware.
   * @param subsystem   either the same as namespace for monolith classes, or the subclass for which we collect metrics,
   *                    e.g., network.latency generator within middleware.
   * @param helpMessage a hint message describing what this metric represents.
   * @param buckets     histogram buckets. The buckets are cumulative,
   *                    i.e., each bucket has a length label that accumulates all
   *                    metrics with a value of less than or equal to length.
   *                    For example, if a bucket has length=0.5 then it means that how many samples have less than or
   *                    equal to 0.5 value.
   */
  public OperaHistogram(String name, String namespace, String subsystem, String helpMessage, double[] buckets) {
    this.histogram = io.prometheus.client.Histogram.build()
      .buckets(buckets)
      .namespace(namespace)
      .name(name)
      .subsystem(subsystem)
      .help(helpMessage)
      .register();
  }


  @Override
  public void observe(Identifier id, double value) {
    this.histogram.labels(id.toString()).observe(value);
  }

  @Override
  public void observe(double value) {
    this.histogram.observe(value);
  }
}
