package Utils;

import Simulator.Simulator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Local static variable between the nodes with a buffer size of 1 (i.e any two consecutive writing on the same variable is forbidden)
 */
public class SharedVariable {
    // for each node ID, hold an array list of all the variables' queues ordered by the variables IDs
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, ArrayDeque<SimpleEntryComparable<UUID, Object>>>> nodeQueues = new ConcurrentHashMap();
    // for each variable ID, hold the cluster of that variable
    private static final ArrayList<ArrayList<UUID>> clusters = new ArrayList<>();
    // for each new variable, assign a new id for it
    private static ConcurrentHashMap<String, Integer> variablesIDs = new ConcurrentHashMap<>();
    // for each variable, keep the lock holder
    private static ArrayList<UUID> lockHolders = new ArrayList<>();
    // for every variable, hold a latch that tell us if a node is currently writing
    private static ArrayList<ReentrantLock> lock = new ArrayList<>();
    private static UUID fixtureOwner = UUID.randomUUID();

    /**
     * Register a new variable in the DSM
     * @param name
     * @param allID the IDs of the nodes that should have access to this variable
     * @return
     */
    public static boolean register(String name, ArrayList<UUID> allID){
        if(variablesIDs.containsKey(name)){
            Simulator.getLogger().debug("[SharedVariable] a variable with name " + name + " is already registered");
            return false;
        }
        // add cluster and assign a new ID
        int variableID = clusters.size();
        variablesIDs.put(name, variableID);
        clusters.add(allID);

        // add new queue for all nodes
        for(UUID nodeID : allID){
            if(!nodeQueues.containsKey(nodeID)){
                nodeQueues.put(nodeID, new ConcurrentHashMap<>());
            }
            nodeQueues.get(nodeID).put(variableID, new ArrayDeque<>());
        }

        // add a lock and assign a random lock holder
        lockHolders.add(fixtureOwner);
        lock.add(new ReentrantLock());
        return true;
    }

    /**
     * Requesting the writing lock for a variable
     * @param nodeID
     * @param name
     * @return
     */
    public static synchronized boolean requestLock(UUID nodeID, String name){
        int variableID = variablesIDs.get(name);
        lock.get(variableID).lock();
        if(getOwner(name).equals(fixtureOwner) || getOwner(name).equals(nodeID)){
            lockHolders.set(variableID, nodeID);
            lock.get(variableID).unlock();
            return true;
        }
        lock.get(variableID).unlock();
        return false;
    }

    /**
     * write a new value to the distributed shared memory with the given name.
     * In case the name is assigned with another type, the variable will be overwritten.
     * @param senderID node UUID
     * @param name
     * @param variable
     * @return Ture in case of success, False otherwise.
     */
    public synchronized static boolean write(UUID senderID, String name, Object variable){
        if(!variablesIDs.containsKey(name)) {
            Simulator.getLogger().debug("[SharedVariable] Write: no variable with name " + name + " is registered");
            return false;
        }
        int variableID = variablesIDs.get(name);
        lock.get(variableID).lock();
        if(!senderID.equals(lockHolders.get(variableID))){
            lock.get(variableID).unlock();
            return false;
        }
        for(UUID nodeID : clusters.get(variableID)){
            if(nodeID != senderID)
                nodeQueues.get(nodeID).get(variableID).addLast(new SimpleEntryComparable<>(senderID, variable));
        }
        lock.get(variableID).unlock();
        return true;
    }


    /**
     * read the value of a DSM variable
     * @param nodeID the reader node ID
     * @param name
     * @return
     */
    public static AbstractMap.SimpleEntry<UUID, Object> read(UUID nodeID, String name) throws NullPointerException{
        if(!variablesIDs.containsKey(name)){
            Simulator.getLogger().error("[SharedVariable] Read: no variable with name " + name + " is registered");
            new ClassNotFoundException("[SharedVariable] Read: no variable with name " + name + " is registered");
        }
        int variableID = variablesIDs.get(name);

        if(!nodeQueues.get(nodeID).containsKey(variableID)){
            Simulator.getLogger().error("[SharedVariable] Read: the node with ID " + nodeID + " does not have " +
                    "access to the variable with name " + name);
            throw new NullPointerException("[SharedVariable] Read: the node with ID " + nodeID + " does not have " +
                    "access to the variable with name " + name);
        }
        if(nodeQueues.get(nodeID).get(variableID).isEmpty()){
            Simulator.getLogger().debug("[SharedVariable] Read: no present values for variable with name " + name);
            return null;
        }
        return nodeQueues.get(nodeID).get(variableID).poll();
    }

    /**
     * get the owner of the lock for a specific variable
     * @param name
     * @return
     */
    public static UUID getOwner(String name){
        int variableID = variablesIDs.get(name);
        lock.get(variableID).lock();
        UUID owner =  lockHolders.get(variableID);
        lock.get(variableID).unlock();
        return owner;
    }

    public static boolean isEmpty(UUID nodeID, String name){
        if(!variablesIDs.containsKey(name)){
            Simulator.getLogger().error("[SharedVariable] Read: no variable with name " + name + " is registered");
            new ClassNotFoundException("[SharedVariable] Read: no variable with name " + name + " is registered");
        }
        int variableID = variablesIDs.get(name);
        if(!nodeQueues.get(nodeID).containsKey(variableID)){
            Simulator.getLogger().error("[SharedVariable] Read: the node with ID " + nodeID + " does not have " +
                    "access to the variable with name " + name);
            throw new NullPointerException("[SharedVariable] Read: the node with ID " + nodeID + " does not have " +
                    "access to the variable with name " + name);
        }
        return nodeQueues.get(nodeID).get(variableID).isEmpty();
    }

    public static void releaseLock(UUID nodeID, String name){
        if(getOwner(name).equals(nodeID)){
            int variableID = variablesIDs.get(name);
            lockHolders.set(variableID, fixtureOwner);
        }
    }
}
