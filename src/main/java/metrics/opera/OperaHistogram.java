package metrics.opera;

import io.prometheus.client.Histogram;
import metrics.Constants;
import metrics.HistogramCollector;
import node.Identifier;

/**
 * OperaHistogram provides a prometheus-based histogram collector.
 */
public class OperaHistogram extends OperaMetric implements HistogramCollector {


  /**
   * Record a new value for the histogram with a specific name and identifier.
   *
   * @param name name of the metric.
   * @param id   identifier of the node.
   * @param v    value to be recorder in histogram.
   */
  @Override
  public void observe(String name, Identifier id, double v) throws IllegalArgumentException {
    Histogram metric = get(name);
    metric.labels(id.toString()).observe(v);
  }

  @Override
  public Histogram get(String name) throws IllegalArgumentException {
    if (!collectors.containsKey(name) || collectorsTypes.get(name) != Type.HISTOGRAM) {
      throw new IllegalArgumentException("Histogram name does not exist: " + name);
    }
    return (Histogram) collectors.get(name);
  }

  /**
   * Registers a histogram collector.
   * This method is expected to be executed by several instances of nodes assuming a decentralized metrics registration.
   * However, only the first invocation gets through and registers the metric. The rest will be gracefully returned.
   * Since the collector is handled globally in a centralized manner behind the scene,
   * only one successful registration is enough.
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
   * @throws IllegalArgumentException when a different metric type (e.g., counter)
   *                                  with the same name has already been registered.
   */
  @Override
  public void register(String name, String namespace, String subsystem, String helpMessage, double[] buckets) throws IllegalArgumentException {

    if (collectors.containsKey(name)) {
      if (collectorsTypes.get(name) != Type.HISTOGRAM) {
        throw new IllegalArgumentException("Metrics name already taken with another type: " + name + " type: " + collectorsTypes.get(name));
      }
      // collector already registered
      return;
    }
    collectors.put(name, Histogram.build().buckets(buckets).namespace(namespace).name(name).help(helpMessage).labelNames(Constants.IDENTIFIER).register());
    collectorsTypes.put(name, Type.HISTOGRAM);
  }
}
