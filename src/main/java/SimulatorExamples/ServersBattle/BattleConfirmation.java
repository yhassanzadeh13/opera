package simulatorexamples.serversbattle;

import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;

/**
 * Is an event to confirm the battle invitations.
 */
public class BattleConfirmation implements Event {

  boolean opponentConfirmation;
  int duration;
  UUID host;
  UUID opponent;
  int opponentLevel;

  public BattleConfirmation(UUID host, UUID opponent, boolean opponentConfirmation) {
    this(host, opponent, opponentConfirmation, 0, 0);
  }

  /**
   * Constructor of BattleConfirmation.
   *
   * @param host ID of the host node
   * @param opponent ID of the opponent
   * @param opponentConfirmation Shows whether opponent accepted battle or not
   * @param duration Duration of the battle
   * @param opponentLevel level of the opponent
   */
  public BattleConfirmation(UUID host, UUID opponent, boolean opponentConfirmation, int duration, int opponentLevel) {
    this.opponentConfirmation = opponentConfirmation;
    this.duration = duration;
    this.host = host;
    this.opponent = opponent;
    this.opponentLevel = opponentLevel;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    Contestant node = (Contestant) hostNode;

    if (this.opponentConfirmation) {
      node.hostFight(this.opponent, this.opponentLevel, this.duration);
    }
    return true;
  }

  @Override
  public String logMessage() {
    if (this.opponentConfirmation) {
      return this.opponent + " confirms " + this.host + " Invitation";
    } else {
      return this.opponent + " declines " + this.host + " Invitation";
    }
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

}
