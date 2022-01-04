package examples.serversbattle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import metrics.MetricsCollector;
import network.MiddleLayer;
import network.packets.Event;
import node.BaseNode;
import node.Identity;
import simulator.Simulator;

/**
 * Contestants are nodes that participate the battles. Which fight over their level.
 */
public class Contestant implements BaseNode {


  public boolean isFighting;
  public boolean isWaiting;
  ReentrantLock lock = new ReentrantLock();
  Network network;
  private UUID selfId;
  private ArrayList<Identity> identities;
  private int healthLevel;
  private ContestantMetrics metrics;

  Contestant() {
  }

  Contestant(UUID selfId, Network network, MetricsCollector metrics) {
    this.selfId = selfId;
    this.network = network;
    this.metrics = new ContestantMetrics(metrics);
  }

  public UUID getId() {
    return this.selfId;
  }

  @Override
  public void onCreate(ArrayList<Identity> identities) {
    Random rand = new Random();
    this.healthLevel = rand.nextInt(30) + 1;
    Simulator.getLogger().info("Contestant " + this.selfId + "was initialized with level " + this.healthLevel);
    this.isFighting = false;
    this.isWaiting = false;
    this.identities = identities;
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
  public BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metrics) {
    return new Contestant(selfId, network, metrics);
  }

  /**
   * Sends new fight invitation to a random node.
   */
  public synchronized void sendNewFightInvitation() {
    if (this.identities.size() <= 1) {
      if (!this.identities.isEmpty() && identities.get(0).equals(this.selfId)) {
        Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
      }
      return;
    }
    Random rand = new Random();
    int duration = rand.nextInt(2000) + 500;
    this.isWaiting = false;
    Collections.shuffle(this.identities);
    int ind = 0;
    while (!this.isWaiting && !this.isFighting) {
      UUID targetId = this.identities.get(ind).getIdentifier();
      if (!this.selfId.equals(targetId)) {
        if (!network.send(targetId, new BattleInvitation(this.selfId, targetId, duration))) {
          this.identities.remove(targetId);
        } else {
          this.isWaiting = true;

          try {
            Thread.sleep(100);
          } catch (Exception e) {
            Simulator.getLogger().error(e.getMessage());
          }
        }
      }
      if (this.identities.size() <= 1) {
        if (!this.identities.isEmpty() && identities.get(0).equals(this.selfId)) {
          Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        }
        return;
      }
      ind += 1;
      if (ind == identities.size()) {
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
   * @param host     Id of the host node
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
   * @param opponent      Id of the opponent
   * @param opponentLevel Level of the opponent
   * @param duration      duration of the battle
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
      this.metrics.onNewFight(this.selfId, opponent, duration);
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
        this.metrics.onHealthUpdate(this.selfId, 10);
        Simulator.getLogger().info(this.selfId + " losses 10 points");
        break;
      case 0:
        this.healthLevel += 1;
        this.metrics.onHealthUpdate(this.selfId, 1);
        Simulator.getLogger().info(this.selfId + " gains 1 point");
        break;
      default:
        this.healthLevel += 5;
        this.metrics.onHealthUpdate(this.selfId, 5);
        Simulator.getLogger().info(this.selfId + " gains 5 points");
        break;
    }
    if (this.healthLevel <= 0) {
      network.done();
      return;
    }

    this.isFighting = false;
    this.isWaiting = false;
    if (this.identities.size() <= 1) {
      if (!this.identities.isEmpty() && identities.get(0).equals(this.selfId)) {
        Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
        System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
      }
      return;
    }

    sendNewFightInvitation();
  }


}
