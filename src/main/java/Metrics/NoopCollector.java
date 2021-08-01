package Metrics;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

import java.util.UUID;

public class NoopCollector implements MetricsCollector{
    class NoopHistogram implements HistogramCollector {

        @Override
        public boolean observe(String name, UUID id, double v) {
            return false;
        }

        @Override
        public Histogram getMetric(String name) {
            return null;
        }

        @Override
        public boolean startTimer(String name, UUID id, String timerID) {
            return false;
        }

        @Override
        public boolean observeDuration(String name, String timerID) {
            return false;
        }

        @Override
        public void tryObserveDuration(String name, String timerID) {

        }

        @Override
        public boolean register(String name) {
            return false;
        }

        @Override
        public boolean register(String name, double[] buckets) {
            return false;
        }
    }

    class NoopGauge implements GaugeCollector {

        @Override
        public boolean register(String name) {
            return false;
        }

        @Override
        public boolean inc(String name, UUID id) {
            return false;
        }

        @Override
        public boolean inc(String name, UUID id, double v) {
            return false;
        }

        @Override
        public boolean dec(String name, UUID id, double v) {
            return false;
        }

        @Override
        public boolean dec(String name, UUID id) {
            return false;
        }

        @Override
        public boolean set(String name, UUID id, double v) {
            return false;
        }

        @Override
        public double get(String name, UUID id) {
            return 0;
        }

        @Override
        public Gauge getMetric(String name) {
            return null;
        }
    }

    class NoopCounter implements CounterCollector {

        @Override
        public boolean inc(String name, UUID id, double v) {
            return false;
        }

        @Override
        public boolean inc(String name, UUID id) {
            return false;
        }

        @Override
        public double get(String name, UUID id) {
            return 0;
        }

        @Override
        public boolean register(String name) {
            return false;
        }
    }

    @Override
    public HistogramCollector Histogram() {
        return new NoopHistogram();
    }

    @Override
    public GaugeCollector Gauge() {
        return new NoopGauge();
    }

    @Override
    public CounterCollector Counter() {
        return new NoopCounter();
    }
}
