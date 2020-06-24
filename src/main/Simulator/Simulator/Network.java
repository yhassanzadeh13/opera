package Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Network {

    private ArrayList<UUID> allID;
    private HashMap<UUID, NodeThread> allInstances;

    public void setAllID(ArrayList<UUID> allID) {
        this.allID = allID;
    }

    public void setAllInstances(HashMap<UUID, NodeThread> allInstances) {
        this.allInstances = allInstances;
    }


    public Network(ArrayList<UUID> allID) {
        this(allID, new HashMap<UUID, NodeThread>());
    }

    public Network(ArrayList<UUID> allID, HashMap<UUID, NodeThread> allInstances) {
        this.allID = allID;
        this.allInstances = allInstances;
    }

    public boolean sendMessage(Message msg)
    {
        if(!allInstances.containsKey(msg.getOriginalID())){
            Simulator.log.error(msg.originalID + ": Node is currently off");
            return false;
        }
        if(!allInstances.containsKey(msg.getTargetID())){
            Simulator.log.error(msg.getTargetID() + ": Node is currently off");
            return false;
        }
        try {

            Event event = (Event) SimulatorUtils.deserialize(msg.getMessage());
            new Thread(new Runnable() {
            @Override
            public void run() {
                allInstances.get(msg.getTargetID()).onNewMessage(msg.getOriginalID(), event);
            }

        }).start();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void stopNode(UUID nodeID) {
        allInstances.get(nodeID).terminate();
        allInstances.remove(nodeID);
    }

    public int getNodeIndex(UUID nodeID)
    {
        return allID.indexOf(nodeID);
    }

    public void addInstance(UUID nodeId, NodeThread instance)
    {
        allInstances.put(nodeId,instance);
    }
}
