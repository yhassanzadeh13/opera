package metrics;

import node.Identifier;

public interface Histogram {
    /**
     * Record a new value for the histogram with a specific name and identifier.
     *
     * @param name name of the metric.
     * @param id   the node id on which the metric will be registered.
     * @param value value by which metric is recorded.
     */
    void observe(String name, Identifier id, double value);
}
