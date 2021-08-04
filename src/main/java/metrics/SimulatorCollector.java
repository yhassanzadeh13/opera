package metrics;

/**
 *
 */
public class SimulatorCollector implements MetricsCollector {
  private final SimulatorCounter mcounter;
  private final SimulatorGauge mgauge;
  private final SimulatorHistogram mhistogram;

  /**
   *
   */
  public SimulatorCollector() {
    this.mcounter = new SimulatorCounter();
    this.mgauge = new SimulatorGauge();
    this.mhistogram = new SimulatorHistogram();
  }

  @Override
  public HistogramCollector histogram() {
    return this.mhistogram;
  }

  @Override
  public GaugeCollector gauge() {
    return this.mgauge;
  }

  @Override
  public CounterCollector counter() {
    return this.mcounter;
  }
}
