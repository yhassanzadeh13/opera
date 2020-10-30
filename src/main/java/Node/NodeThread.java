package Node;

import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class NodeThread<T extends BaseNode> extends Thread {


    BaseNode Node;
    UUID selfID;
    ArrayList<UUID> allID;
    private final AtomicBoolean running = new AtomicBoolean(false);


    public NodeThread(T factory, UUID selfID, ArrayList<UUID> allID, MiddleLayer middleLayer) {
        this.selfID = selfID;
        this.allID = allID;
        Node = factory.newInstance(selfID, middleLayer);
    }

    @Override
    public void run() {
        while(running.get())
        {
            continue;
        }
    }

    public void terminate()
    {
        Node.onStop();
        running.set(false);
    }

    public void onNewMessage(UUID originID, Event msg)
    {
        Node.onNewMessage(originID, msg);
    }

    public void onCreate(ArrayList<UUID> allID)
    {
        Node.onCreate(allID);
    }

    public void onStart()
    {
        Node.onStart();
    }

    public void onStop()
    {
        Node.onStop();
    }

}
