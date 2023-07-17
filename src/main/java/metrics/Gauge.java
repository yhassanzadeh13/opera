package metrics;

import node.Identifier;

/**
 * A base interface for gauge collector.
 */
public interface Gauge {
    /**
     * Set the gauge with a specific name and identifier.
     *
     * @param id    the node id on which the metric will be registered.
     * @param value value by which metric is set.
     */
    void set(Identifier id, double value);

    /**
     * Set the gauge with a specific name and identifier.
     *
     * @param value value by which metric is set.
     */
    void set(double value);
}
