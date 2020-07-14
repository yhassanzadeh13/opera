
package SimulatorExamples.HelloServers;

import Simulator.BaseNode;
import Simulator.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class myNode implements BaseNode {


    UUID selfID;
    ArrayList<UUID> allID;

    myNode(UUID selfID){
        this.selfID = selfID;
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allID = allID;
        Simulator.Ready(selfID);
    }

    @Override
    public void onStart() {

        this.sendNewMessage("Hello");
    }

    public void sendNewMessage(String msg)
    {
        if(allID.isEmpty())
            return;
        Random rand = new Random();
        int ind = rand.nextInt(allID.size());
        SendHello helloMessage = new SendHello(msg, selfID, allID.get(ind));
        Simulator.Submit(selfID, allID.get(ind), helloMessage);
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
            Random rand = new Random();
            Thread.sleep(rand.nextInt(1000));
        }catch (InterruptedException e){}
        msg.actionPerformed(this);
    }
}
