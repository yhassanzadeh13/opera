package examples.serversbattle;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import metrics.Constants;
import metrics.MetricsCollector;

/**
 * ContestantMetrics is a metrics collector for the server battles demo. The purpose of this class is
 * to represent the correct way of collecting metrics in Opera.
 */
public class ContestantMetrics {
  private static final String SUBSYSTEM_SERVER_BATTLE = "server_battle";
  private static final ReentrantLock lock = new ReentrantLock();
  private static final UUID collectorID = UUID.randomUUID();
  private static MetricsCollector metricsCollector;

  /**
   * Creates a metric collector for server battle demo.
   *
   * @param metricsCollector root metric collector of opera.
   */
  public ContestantMetrics(MetricsCollector metricsCollector) {
    if (!lock.tryLock()) {
      // another thread is initiating
      return;
    }
    if (ContestantMetrics.metricsCollector != null) {
      // already initialized
      lock.unlock();
      return;
    }

    ContestantMetrics.metricsCollector = metricsCollector;

    // metrics registration
    ContestantMetrics.metricsCollector.gauge().register(
        Name.HEALTH_LEVEL,
        Constants.Namespace.DEMO,
        SUBSYSTEM_SERVER_BATTLE,
        HelpMsg.HEALTH_LEVEL);

    ContestantMetrics.metricsCollector.counter().register(
        Name.FIGHT_COUNT,
        Constants.Namespace.DEMO,
        SUBSYSTEM_SERVER_BATTLE,
        HelpMsg.FIGHT_COUNT);

    ContestantMetrics.metricsCollector.histogram().register(
        Name.FIGHT_DURATION,
        Constants.Namespace.DEMO,
        SUBSYSTEM_SERVER_BATTLE,
        HelpMsg.FIGHT_DURATION,
        new double[]{500.0, 1000.0, 1500.0, 2000.0, 2500.0});

    lock.unlock();
  }

  /**
   * OnNewFight is called whenever two nodes start a fight, it increases the
   * number of fights each node made by one. It also records their fight duration.
   *
   * @param uuid1 identifier of first node
   * @param uuid2 identifier of second node
   */
  public void onNewFight(UUID uuid1, UUID uuid2, double duration) {
    ContestantMetrics.metricsCollector.counter().inc(Name.FIGHT_COUNT, uuid1);
    ContestantMetrics.metricsCollector.counter().inc(Name.FIGHT_COUNT, uuid2);
    ContestantMetrics.metricsCollector.histogram().observe(Name.FIGHT_DURATION, collectorID, duration);
  }

  /**
   * OnHealthUpdate is invoked whenever health level of a contestant is changed.
   * It updates the gauge value for that contestant.
   *
   * @param uuid        contestant id.
   * @param healthValue updated health value.
   */
  public void onHealthUpdate(UUID uuid, int healthValue) {
    ContestantMetrics.metricsCollector.gauge().set(Name.HEALTH_LEVEL, uuid, healthValue);
  }

  private static class Name {
    private static final String FIGHT_COUNT = "fight_count_total";
    private static final String FIGHT_DURATION = "fight_duration_ms";
    private static final String HEALTH_LEVEL = "health_level";
  }

  private static class HelpMsg {
    public static final String FIGHT_COUNT = "total fights a contestant makes";
    private static final String HEALTH_LEVEL = "health level of contestant";
    private static final String FIGHT_DURATION = "fight duration of contestants";
  }
}
