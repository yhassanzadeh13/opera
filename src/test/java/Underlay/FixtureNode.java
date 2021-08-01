package Underlay;

import Metrics.MetricsCollector;
import Node.BaseNode;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FixtureNode implements BaseNode {
    private final UUID selfID;
    private final ArrayList<UUID> allID;
    private final MiddleLayer network;
    public AtomicInteger receivedMessages = new AtomicInteger(0);

    FixtureNode(UUID selfID, ArrayList<UUID> allID, MiddleLayer network){
        this.selfID = selfID;
        this.network = network;
        this.allID = allID;
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
    }

    @Override
    public void onStart() {
        for(UUID id : allID){
            if(id != selfID)
                network.send(id, new FixtureEvent());
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public BaseNode newInstance(UUID ID, MiddleLayer network, MetricsCollector metrics) {
        return null;
    }

    @Override
    public void onNewMessage(UUID originID, Event msg){
        this.receivedMessages.incrementAndGet();
    }
}
