package utils;

import network.model.Event;
import node.BaseNode;
import node.Identifier;

import java.util.ArrayList;

/**
 * A basic BaseNode to check whether Utils coded correctly or not.
 */
public class FixtureNode implements BaseNode {
    private Identifier selfId;
    private ArrayList<Identifier> allId;
    private network.Network network;

    public FixtureNode() {
    }

    FixtureNode(Identifier selfId, network.Network network) {
        this.selfId = selfId;
        this.network = network;
    }


    @Override
    public void onCreate(ArrayList<Identifier> allId) {
        this.allId = allId;
        network.ready();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        this.network.stop();
    }

    @Override
    public BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network) {
        return new FixtureNode(selfId, network);
    }

    @Override
    public void onNewMessage(Identifier originId, Event msg) {

    }
}
