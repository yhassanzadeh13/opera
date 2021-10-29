package examples.serversbattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import metrics.MetricsCollector;
import node.BaseNode;
import simulator.Simulator;
import underlay.MiddleLayer;
import underlay.packets.Event;

/**
 * Contestants are nodes that participate the battles. Which fight over their level.
 */
public class Contestant implements BaseNode {

  static final String FIGHTCOUNT = "FightCount";
  static final String FIGHTDURATION = "FightDuration";
  static final String HEALTHLEVEL = "HealthLevel";
  public boolean isFighting;
  public boolean isWaiting;
  ReentrantLock lock = new ReentrantLock();
  MiddleLayer network;
  private UUID selfId;
  private ArrayList<UUID> allId;
  private int healthLevel;
  private MetricsCollector metrics;

  Contestant() {
  }

  Contestant(UUID selfId, MiddleLayer network) {
    this.selfId = selfId;
    this.network = network;

    //Register metrics
    this.metrics.gauge().register(HEALTHLEVEL);
    this.metrics.counter().register(FIGHTCOUNT);
    this.metrics.histogram().register(FIGHTDURATION, new double[]{500.0, 1000.0, 1500.0, 2000.0, 2500.0});
  }

  public UUID getId() {
    return this.selfId;
  }

  boolean isFighting() {
    return this.isFighting;
  }

  int getHealthLevel() {
    return this.healthLevel;
  }

  @Override
  public void onCreate(ArrayList<UUID> allId) {

    Random rand = new Random();
    this.healthLevel = rand.nextInt(30) + 1;
    Simulator.getLogger().info("Contestant " + this.selfId + "was initialized with level " + this.healthLevel);
    this.isFighting = false;
    this.isWaiting = false;
    this.allId = allId;
    network.ready();
  }

  @Override
  public void onStart() {
    this.sendNewFightInvitation();
  }

  @Override
  public void onStop() {
    System.out.println("Contestant " + this.selfId + " says goodbye");
  }

  @Override
  public void onNewMessage(UUID originId, Event msg) {
    msg.actionPerformed(this);
  }

  @Override
  public BaseNode newInstance(UUID selfId, MiddleLayer network) {
    return new Contestant(selfId, network);
  }

  /**
   * Sends new fight invitation to a random node.
   */
  public synchronized void sendNewFightInvitation() {
    if (this.allId.size() <= 1) {
      if (!this.allId.isEmpty() && allId.get(0).equals(this.selfId)) {
        Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
      }
      return;
    }
    Random rand = new Random();
    int duration = rand.nextInt(2000) + 500;
    this.isWaiting = false;
    Collections.shuffle(this.allId);
    int ind = 0;
    while (!this.isWaiting && !this.isFighting) {
      UUID targetId = this.allId.get(ind);
      if (!this.selfId.equals(targetId)) {
        if (!network.send(targetId, new BattleInvitation(this.selfId, targetId, duration))) {
          this.allId.remove(targetId);
        } else {
          this.isWaiting = true;

          try {
            Thread.sleep(100);
          } catch (Exception e) {
            Simulator.getLogger().error(e.getMessage());
          }
        }
      }
      if (this.allId.size() <= 1) {
        if (!this.allId.isEmpty() && allId.get(0).equals(this.selfId)) {
          Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        }
        return;
      }
      ind += 1;
      if (ind == allId.size()) {
        try {
          Thread.sleep(500);
        } catch (Exception e) {
          Simulator.getLogger().error("Thread cannot sleep" + e.getMessage());
        }
        ind = 0;
      }
    }
  }

  /**
   * If a new fight invitation arrives node calls this function and accepts the invitation if node is available.
   *
   * @param host Id of the host node
   * @param duration duration of the battle
   */
  public void onNewFightInvitation(UUID host, int duration) {
    if (!this.isFighting) {
      if (network.send(host, new BattleConfirmation(host, this.selfId, true, duration, this.healthLevel))) {
        this.isFighting = true;
      } else {
        Simulator.getLogger().debug(this.selfId + "could not reach " + host);
      }
    } else {
      network.send(host, new BattleConfirmation(host, this.selfId, false));
    }
  }

  /**
   * If hosts level is bigger than opponents health host wins
   * if opponents level is bigger than hosts health opponent wins.
   *
   * @param opponent Id of the opponent
   * @param opponentLevel Level of the opponent
   * @param duration duration of the battle
   */
  public synchronized void hostFight(UUID opponent, int opponentLevel, int duration) {
    if (this.isFighting) {
      network.send(opponent, new BattleResult(this.selfId, opponent, true));
    } else if (opponentLevel > 0 && this.healthLevel > 0) {
      lock.lock();
      try {
        this.isFighting = true;
      } finally {
        lock.unlock();
      }

      Simulator.getLogger().info(this.selfId + " is fighting against " + opponent);
      try {
        Thread.sleep(duration);
      } catch (Exception e) {
        Simulator.getLogger().error(e.getMessage());
      }

      int res = 0;
      if (this.healthLevel > opponentLevel) {
        res = 1;
      } else if (this.healthLevel < opponentLevel) {
        res = -1;
      }
      System.out.println("New fighting is happening between contestant with level " + opponentLevel
            + " and contestant with level " + this.healthLevel);
      network.send(opponent, new BattleResult(this.selfId, opponent, false, res * -1));

      // update metrics
      this.metrics.counter().inc(FIGHTCOUNT, this.selfId);
      this.metrics.counter().inc(FIGHTCOUNT, opponent);
      this.metrics.histogram().observe(FIGHTDURATION, this.selfId, duration);
      this.metrics.histogram().observe(FIGHTDURATION, opponent, duration);
      updateHealth(res);
    }
  }

  /**
   * if loses health reduces by 10.
   * if draw health increases by 1.
   * if win health increases by 10.
   *
   * @param result result of the battle
   */
  public synchronized void updateHealth(int result) {
    switch (result) {
      case -1:
        this.healthLevel -= 10;
        this.metrics.gauge().dec(HEALTHLEVEL, this.selfId, 10);
        Simulator.getLogger().info(this.selfId + " losses 10 points");
        break;
      case 0:
        this.healthLevel += 1;
        this.metrics.gauge().inc(HEALTHLEVEL, this.selfId, 1);
        Simulator.getLogger().info(this.selfId + " gains 1 point");
        break;
      default:
        this.healthLevel += 5;
        this.metrics.gauge().inc(HEALTHLEVEL, this.selfId, 5);
        Simulator.getLogger().info(this.selfId + " gains 5 points");
        break;
    }
    if (this.healthLevel <= 0) {
      network.done();
      return;
    }

    this.isFighting = false;
    this.isWaiting = false;
    if (this.allId.size() <= 1) {
      if (!this.allId.isEmpty() && allId.get(0).equals(this.selfId)) {
        Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
      }
      return;
    }

    sendNewFightInvitation();
  }


}
