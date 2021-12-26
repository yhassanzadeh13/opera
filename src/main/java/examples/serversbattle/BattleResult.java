package examples.serversbattle;

import java.util.UUID;

import network.packets.Event;
import node.BaseNode;

/**
 * BattleResult is an event to declare the result of the battle between two nodes.
 */
public class BattleResult implements Event {

  int result;
  boolean aborted;
  UUID host;
  UUID opponent;


  public BattleResult(UUID host, UUID opponent, boolean aborted) {
    this(host, opponent, aborted, 0);
  }

  /**
   * Constructor of BattleResult.
   *
   * @param host     Id of the host node
   * @param opponent Id of the opponent node
   * @param aborted  shows whether battle is aborted or not
   * @param result   Result of the battle
   */
  public BattleResult(UUID host, UUID opponent, boolean aborted, int result) {
    this.result = result;
    this.host = host;
    this.aborted = aborted;
    this.opponent = opponent;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    Contestant node = (Contestant) hostNode;
    if (aborted) {
      node.isFighting = false;
      node.sendNewFightInvitation();
    } else {
      node.updateHealth(result);
    }

    return true;
  }

  @Override
  public String logMessage() {
    if (aborted) {
      return this.host + " aborted the game";
    } else if (result == 1) {
      return this.host + " defeated " + this.opponent;
    } else if (result == -1) {
      return this.opponent + " defeated " + this.host;
    } else {
      return this.host + " draw with " + this.opponent;
    }
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
