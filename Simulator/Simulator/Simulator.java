package Simulator;

import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Simulator<T extends BaseNode> {



    //Constants
    private final int MAX_PORT = 65535;
    private final int MIN_PORT = 2000;

    //Simulator.Simulator config
    private static int nodeCnt = 0;
    private static boolean isLocal = true;
    private static ArrayList<UUID> allID;
    private static HashMap<UUID, NodeThread> allInstances;
    private static HashMap<UUID, Boolean> isReady;
    private T factory;
    CountDownLatch latch;

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
        allInstances = new HashMap<UUID, NodeThread>();

        if(!isLocal && allID.size() != N)throw new InputMismatchException();
        if(isLocal)this.generateNodes();
        latch = new CountDownLatch(nodeCnt);
    }

    private void generateNodes() {
        Random rnd = new Random();
        int ports[] = rnd.ints(2*nodeCnt, MIN_PORT, MAX_PORT).distinct().limit(nodeCnt).toArray();
        for(int i = 0;i<nodeCnt;i++)
            //allID.add(UUID.fromString("localhost:" + ports[i]));
            allID.add(UUID.randomUUID());

        for(int i = 0;i<nodeCnt;i++)
        {
            isReady.put(allID.get(i), false);
            NodeThread<T> Node = new NodeThread<T>(factory, allID.get(i), allID);
            allInstances.put(allID.get(i), Node);
        }
    }

    public static boolean Submit(UUID originalID, UUID targetID, Message msg)
    {
        if(!allInstances.containsKey(originalID) || !allInstances.containsKey((targetID)))return false;
        if(!isReady.get(originalID) || !isReady.get((targetID)))return false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                allInstances.get(targetID).onNewMessage(originalID, msg);
            }
        }).start();
        return true;
    }

    public static void Ready(UUID nodeID)
    {
        isReady.put(nodeID, true);
    }

    public static void Done(UUID nodeID)
    {
        allInstances.get(nodeID).terminate();
        allInstances.remove(nodeID);
        isReady.remove(nodeID);
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
                    Message msg = new Message(s.nextLine());
                    System.out.println(Submit(allID.get(idA), allID.get(idB), msg));
                    break;
            }
            query = s.nextInt();
        }
    }

}
