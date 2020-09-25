package SimulatorExamples.ServersBattle;

import Node.BaseNode;
import Underlay.packets.Event;

import java.util.UUID;

public class BattleResult implements Event {

    int result;
    boolean aborted;
    UUID host, opponent;


    public BattleResult(UUID host, UUID opponent, boolean aborted) {
        this(host, opponent, aborted, 0);
    }

    public BattleResult(UUID host, UUID opponent, boolean aborted, int result) {
        this.result = result;
        this.host = host;
        this.aborted = aborted;
        this.opponent = opponent;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        Contestant node = (Contestant) hostNode;
        if(aborted){
            node.isFighting = false;
            node.sendNewFightInvitation();
        }
        else{
            node.updateHealth(result);
        }

        return true;
    }

    @Override
    public String logMessage() {
        if (aborted)
            return this.host + " aborted the game";
        else if(result == 1)
            return this.host + " defeated " + this.opponent;
        else if(result == -1)
            return this.opponent + " defeated " + this.host;
        else return this.host + " draw with " + this.opponent;
    }
}
