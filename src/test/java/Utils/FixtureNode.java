package Utils;

import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.UUID;

public class FixtureNode implements BaseNode {
    private UUID selfID;
    private ArrayList<UUID> allID;
    private MiddleLayer network;
    public int receivedMessages = 0;

    FixtureNode(){}

    FixtureNode(UUID selfID, MiddleLayer network){
        this.selfID = selfID;
        this.network = network;
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allID = allID;
        network.ready();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
    }

    @Override
    public BaseNode newInstance(UUID ID, MiddleLayer network) {
        return new FixtureNode(ID, network);
    }

    @Override
    public void onNewMessage(UUID originID, Event msg){

    }
}
