package Simulator;

import SimulatorTest.SendHello;
import java.util.*;
import org.apache.log4j.Logger;


public class Simulator<T extends BaseNode> {

    //Simulator config
    private static int nodeCnt = 0;
    private static boolean isLocal = true;
    private ArrayList<UUID> allID;
    private static HashMap<UUID, Boolean> isReady;
    private T factory;
    private static Network network;
    public static Logger log = Logger.getLogger(Simulator.class.getName());

    public Simulator(T factory, int N)
    {
        this(factory, N, true);
    }

    public Simulator(T factory, int N, boolean isLocal)
    {
        this(factory, N, isLocal, new ArrayList<UUID>());
    }

    public Simulator(T factory, int N, boolean isLocal, ArrayList<UUID> allID)
    {
        this.factory = factory;
        this.nodeCnt = N;
        this.isLocal = isLocal;
        this.allID = allID;
        isReady = new HashMap<UUID, Boolean>();
        if(!isLocal && allID.size() != N)throw new InputMismatchException();

        network = new Network(allID, new HashMap<UUID, NodeThread>());
        if(isLocal)this.generateNodes();
    }

    private void generateNodes() {
        for(int i = 0;i<nodeCnt;i++)
            allID.add(UUID.randomUUID());

        network.setAllID(allID);
        for(int i = 0;i<nodeCnt;i++)
        {
            isReady.put(allID.get(i), false);
            NodeThread<T> node = new NodeThread<T>(factory, allID.get(i), allID);
            network.addInstance(allID.get(i), node);
        }
    }


    public static boolean Submit(UUID originalID, UUID targetID, Event msg)
    {
        if(!isReady.get(originalID)){
            Simulator.log.error(originalID + ": Node is not ready yet..");
            return false;
        }
        if(!isReady.get(targetID)){
            Simulator.log.error(targetID + ": Node is not ready yet..");
            return false;
        }
        try{
            byte[] tmp = SimulatorUtils.serialize(msg);
            boolean sent =  network.sendMessage(new Message(originalID, targetID, tmp));
            if(sent)
            {
                log.info(originalID + ": an event " + msg.logMessage() + " was sent to node " + targetID);
                return true;
            }
            else
                log.error(originalID + ": event " + msg.logMessage() + " was not able to be sent to node " + targetID);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void Ready(UUID nodeID)
    {
        isReady.put(nodeID, true);
        log.info(nodeID + ": node is ready");
    }

    public static void Done(UUID nodeID)
    {
        log.info(nodeID + ": node is terminating");
        isReady.put(nodeID, false);
        network.stopNode(nodeID);
    }

    public static int getNodeIndex(UUID nodeID)
    {
        return network.getNodeIndex(nodeID);
    }

    public void start()
    {
        System.out.println("Simulation is ready...");
        System.out.println("Simulation servers IDs:");
        System.out.println(allID);
        System.out.println("The following operations are available:\n" +
                "1 nodeIndex: turn down a node in the cluster\n" +
                "2 originalIndex TargetIndex msg: send a message from originalID to targetID\n" +
                "3 to terminate");
        Scanner s = new Scanner(System.in);
        int query = s.nextInt();
        System.out.println(query);
        while(query != 3)
        {
            int idA = s.nextInt();
            switch (query)
            {
                case 1:
                    Done(allID.get(idA));
                    break;
                case 2:
                    int idB = s.nextInt();
                    SendHello msg = new SendHello("Hello", allID.get(idA), allID.get(idB));
                    System.out.println(Submit(allID.get(idA), allID.get(idB), msg));
                    break;
            }
            query = s.nextInt();
        }
    }

}
