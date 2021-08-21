package examples.serversbattle;

import java.util.UUID;
import node.BaseNode;
import underlay.packets.Event;

/**
 * BattleInvitation is an event to send battle invitations to another nodes.
 */
public class BattleInvitation implements Event {
  UUID host;
  UUID opponent;
  int duration;

  /**
   * Constructor of the BattleInvitation.
   *
   * @param host Id of the host node
   * @param opponent Id of the reciever of the invitation
   * @param duration duration of the battle
   */
  public BattleInvitation(UUID host, UUID opponent, int duration) {
    this.host = host;
    this.opponent = opponent;
    this.duration = duration;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    Contestant node = (Contestant) hostNode;
    node.onNewFightInvitation(host, duration);
    return true;
  }

  @Override
  public String logMessage() {
    return this.host + " Invitation is pending " + this.opponent + " Confirmation";
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
