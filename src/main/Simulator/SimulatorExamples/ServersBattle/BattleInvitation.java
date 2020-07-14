package SimulatorExamples.ServersBattle;

import Simulator.BaseNode;
import Simulator.Event;

import java.util.UUID;

public class BattleInvitation implements Event {

    UUID host, opponent;
    int duration;

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
}
