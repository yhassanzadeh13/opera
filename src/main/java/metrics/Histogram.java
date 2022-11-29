package metrics;

import node.Identifier;

public interface Histogram {
    /**
     * Record a new value for the histogram with a specific name and identifier.
     *
     * @param id   the node id on which the metric will be registered.
     * @param value value by which metric is recorded.
     */
    void observe(Identifier id, double value);
}
