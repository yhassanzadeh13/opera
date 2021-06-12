package SimulatorExamples.BullyAlgorithm;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.MiddleLayer;
import java.util.Collections;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class MyNode implements BaseNode{
    final int NO_RESPONSE_SPAN = 5000 ;
    private static final String MESSAGE_COUNT = "MessageCnt";
    private UUID selfID;
    private ArrayList<UUID> allID;
    private MiddleLayer network;
    private UUID coordinatorID;

    MyNode(){}
    MyNode(UUID selfID, MiddleLayer network){
        this.selfID = selfID;
        this.network = network;

        //Register metrics
        SimulatorHistogram.register("packetSize", new double[]{1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 20.0});
        }
    public boolean isMax(){
        UUID maxUUID = Collections.max(allID);
        return maxUUID == this.selfID;
    }
    public void sendNewMessageToAll() {
        if(allID.isEmpty())
            return;
        for (int i=0; i<allID.size(); i++){
            UUID targetID = allID.get(i);
            Message victoryMassage = new Message("Victory", selfID, targetID);
            network.send(targetID, victoryMassage);
        }
    }

    public void sendNewMessagetoBigger() {
        if(allID.isEmpty())
            return;
        for (int i=0; i<allID.size(); i++){
            UUID targetID = allID.get(i);
            if(targetID.compareTo(selfID) == 1) {
                Message electionMassage = new Message("Election", selfID, targetID);
                network.send(targetID, electionMassage);
            }
        }
    }

    public void setCoordinatorID(UUID uuıd){
    this.coordinatorID = uuıd;
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allID = allID;
        network.ready();
    }

    @Override
    public void onStart() {
        if (this.isMax()){
            this.sendNewMessageToAll();
        }
        else{
            this.sendNewMessagetoBigger();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public BaseNode newInstance(UUID ID, MiddleLayer network) {
        return new MyNode(ID, network);
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
