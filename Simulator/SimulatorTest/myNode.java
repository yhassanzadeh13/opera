
package SimulatorTest;

import Simulator.BaseNode;
import Simulator.*;

import java.util.ArrayList;
import java.util.UUID;

public class myNode implements BaseNode {


    UUID selfID;
    ArrayList<UUID> allID;
    static int cnt = 0;

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
        cnt = 0;
    }

    @Override
    public BaseNode newInstance(UUID ID) {
        return new myNode(ID);
    }

    @Override
    public void onNewMessage(UUID originID, Message msg){
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){}
        if(cnt < 3)
            for(int i = 0;i<allID.size();i++)
                if(allID.get(i) != selfID)
                    Simulator.Submit(selfID, allID.get(i), new myMessage("Hello"));
        cnt++;
    }
}
