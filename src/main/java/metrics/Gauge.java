package metrics;

import node.Identifier;

/**
 * A base interface for gauge collector.
 */
public interface Gauge {
    /**
     * Set the gauge with a specific name and identifier.
     *
     * @param name name of the metric.
     * @param id   the node id on which the metric will be registered.
     * @param value value by which metric is set.
     */
    void set(String name, Identifier id, double value);
}
