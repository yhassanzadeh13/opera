package SimulatorExamples.ServersBattle;

import Simulator.BaseNode;
import Simulator.Event;

import java.util.UUID;

public class BattleConfirmation implements Event{

    boolean opponentConfirmation;
    int duration;
    UUID host, opponent;
    int opponentLevel;

    public BattleConfirmation(UUID host, UUID opponent, boolean opponentConfirmation) {
        this(host, opponent, opponentConfirmation, 0, 0);
    }

    public BattleConfirmation(UUID host, UUID opponent, boolean opponentConfirmation, int duration,  int opponentLevel) {
        this.opponentConfirmation = opponentConfirmation;
        this.duration = duration;
        this.host = host;
        this.opponent = opponent;
        this.opponentLevel = opponentLevel;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        Contestant node = (Contestant) hostNode;

        if(this.opponentConfirmation)
            node.hostFight(this.opponent, this.opponentLevel, this.duration);

        return true;
    }

    @Override
    public String logMessage() {
        if(this.opponentConfirmation)
            return this.opponent + " confirms " + this.host + " Invitation";
        else
            return this.opponent + " declines " + this.host + " Invitation";
    }

}
