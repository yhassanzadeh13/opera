
package SimulatorTest;

import Simulator.BaseNode;
import Simulator.*;

import java.util.ArrayList;
import java.util.UUID;

public class myNode implements BaseNode {


    UUID selfID;
    ArrayList<UUID> allID;

    myNode(UUID selfID){
        this.selfID = selfID;
    }

    @Override
    public void onStart(ArrayList<UUID> allID) {
        this.allID = allID;
        Simulator.Ready(selfID);
    }

    @Override
    public void onStop() {
    }

    @Override
    public BaseNode newInstance(UUID ID) {
        return new myNode(ID);
    }

    @Override
    public void onNewMessage(UUID originID, Event msg){
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){}
        msg.actionPerformed();
    }
}
