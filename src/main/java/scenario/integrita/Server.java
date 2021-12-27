package scenario.integrita;

import metrics.MetricsCollector;
import node.BaseNode;
import underlay.MiddleLayer;
import underlay.packets.Event;

import java.util.ArrayList;
import java.util.UUID;

public class Server implements BaseNode {
    UUID id;
    MiddleLayer network;
    ArrayList<UUID> ids; // all ids including self

    public Server(){

    }
    public Server(UUID selfId, MiddleLayer network){
        this.id = selfId;
        this.network = network;
    }

    @Override
    public void onCreate(ArrayList<UUID> allId) {
        this.ids = allId;
        this.network.ready();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onNewMessage(UUID originId, Event msg) {
        System.out.println("Sender UUID: " + originId.toString() + " message " + msg.logMessage());
    }

    @Override
    public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
        Server server = new Server(selfId, network);
        return server;
    }
}
