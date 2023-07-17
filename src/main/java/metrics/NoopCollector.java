package metrics;

import node.Identifier;

/**
 * Noop Collector is a no operation metric collector.
 */
public class NoopCollector {
    static class NoopHistogram implements Histogram {
        /**
         * Record a new value for the histogram with a specific name and identifier.
         *
         * @param id    the node id on which the metric will be registered.
         * @param value value by which metric is recorded.
         */
        @Override
        public void observe(Identifier id, double value) {

        }

        /**
         * Record a new value for the histogram with a specific name and identifier.
         *
         * @param value value by which metric is recorded.
         */
        @Override
        public void observe(double value) {

        }
    }

    static class NoopGauge implements Gauge {
        /**
         * Set the gauge with a specific name and identifier.
         *
         * @param id    the node id on which the metric will be registered.
         * @param value value by which metric is set.
         */
        @Override
        public void set(Identifier id, double value) {
            // do nothing
        }

        /**
         * Set the gauge with a specific name and identifier.
         *
         * @param value value by which metric is set.
         */
        @Override
        public void set(double value) {
            // do nothing
        }
    }

    static class NoopCounter implements Counter {
        /**
         * Increment the counter with a specific name and identifier.
         *
         * @param id the node id on which the metric will be registered.
         */
        @Override
        public void increment(Identifier id) {
            // do nothing
        }

        /**
         * Increment the counter with a specific name and identifier by a specific value.
         *
         * @param name  name of the metric.
         * @param id    the node id on which the metric will be registered.
         * @param value value by which metric is incremented.
         */
        @Override
        public void increment(String name, Identifier id, double value) {
            // do nothing
        }

        /**
         * Decrement the counter with a specific name and identifier.
         *
         * @param name name of the metric.
         * @param id   the node id on which the metric will be registered.
         */
        @Override
        public void decrement(String name, Identifier id) {
            // do nothing
        }


        /**
         * Decrement the counter with a specific name and identifier by a specific value.
         *
         * @param name  name of the metric.
         * @param id    the node id on which the metric will be registered.
         * @param value value by which metric is decremented.
         */
        @Override
        public void decrement(String name, Identifier id, double value) {
            // do nothing
        }
    }
}
