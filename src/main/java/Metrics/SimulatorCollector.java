package Metrics;

public class SimulatorCollector implements MetricsCollector {
    private final SimulatorCounter mCounter;
    private final SimulatorGauge mGauge;
    private final SimulatorHistogram mHistogram;

    public SimulatorCollector() {
        this.mCounter = new SimulatorCounter();
        this.mGauge = new SimulatorGauge();
        this.mHistogram = new SimulatorHistogram();
    }

    @Override
    public HistogramCollector Histogram() {
        return this.mHistogram;
    }

    @Override
    public GaugeCollector Gauge() {
        return this.mGauge;
    }

    @Override
    public CounterCollector Counter() {
        return this.mCounter;
    }
}
