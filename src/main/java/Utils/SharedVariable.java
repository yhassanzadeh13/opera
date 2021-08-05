package utils;

//import java.util.*;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import simulator.Simulator;

/**
 * local static variable between the nodes with a buffer size of 1
 * (any two consecutive writing on the same variable is forbidden).
 */
public class SharedVariable {
  // for each node ID, hold an array list of all the variables' queues ordered by the variables IDs

  private final ConcurrentHashMap<UUID,
        ConcurrentHashMap<Integer,
              ArrayDeque<SimpleEntryComparable<UUID,
                    Object>>>> nodeQueues;
  // for each variable ID, hold the cluster of that variable
  private final ArrayList<ArrayList<UUID>> clusters;
  // for each new variable, assign a new id for it
  private ConcurrentHashMap<String, Integer> variablesIds;
  // for each variable, keep the lock holder
  private ArrayList<UUID> lockHolders;
  // for every variable, hold a latch that tell us if a node is currently writing
  private ArrayList<ReentrantLock> lock;
  private UUID fixtureOwner;

  // singleton instance
  private static SharedVariable instance = null;

  private SharedVariable() {
    nodeQueues = new ConcurrentHashMap<>();
    clusters = new ArrayList<>();
    variablesIds = new ConcurrentHashMap<>();
    lockHolders = new ArrayList<>();
    lock = new ArrayList<>();
    fixtureOwner = UUID.randomUUID();

  }

  /**
   * Getter of instance of shared variable.
   *
   * @return the instance of the shared variable
   */
  public static SharedVariable getInstance() {
    if (SharedVariable.instance == null) {
      instance = new SharedVariable();
    }
    return SharedVariable.instance;
  }

  /**
   * Register a new variable in the DSM.
   *
   * @param name name of the variable
   * @param allId the IDs of the nodes that should have access to this variable
   * @return false if variable already registered, true otherwise
   */
  public boolean register(String name, ArrayList<UUID> allId) {
    if (variablesIds.containsKey(name)) {
      Simulator.getLogger().debug("[SharedVariable] a variable with name " + name + " is already registered");
      return false;
    }
    // add cluster and assign a new ID
    int variableId = clusters.size();
    variablesIds.put(name, variableId);
    clusters.add(allId);

    // add new queue for all nodes
    for (UUID nodeId : allId) {
      if (!nodeQueues.containsKey(nodeId)) {
        nodeQueues.put(nodeId, new ConcurrentHashMap<>());
      }
      nodeQueues.get(nodeId).put(variableId, new ArrayDeque<>());
    }

    // add a lock and assign a random lock holder
    lockHolders.add(fixtureOwner);
    lock.add(new ReentrantLock());
    return true;
  }

  /**
   * Requesting the writing lock for a variable.
   *
   * @param nodeId Id of the node
   * @param name name of the variable
   * @return true if node equal to fixture owner or nodeID
   */
  public synchronized boolean requestLock(UUID nodeId, String name) {
    int variableId = variablesIds.get(name);
    lock.get(variableId).lock();
    if (getOwner(name).equals(fixtureOwner) || getOwner(name).equals(nodeId)) {
      lockHolders.set(variableId, nodeId);
      lock.get(variableId).unlock();
      return true;
    }
    lock.get(variableId).unlock();
    return false;
  }

  /**
   * write a new value to the distributed shared memory with the given name.
   * In case the name is assigned with another type, the variable will be overwritten.
   *
   * @param senderId node UUID
   * @param name name of the value
   * @param variable variable to overwrite
   * @return Ture in case of success, False otherwise.
   */
  public synchronized boolean write(UUID senderId, String name, Object variable) {
    if (!variablesIds.containsKey(name)) {
      Simulator.getLogger().debug("[SharedVariable] Write: no variable with name " + name + " is registered");
      return false;
    }
    int variableId = variablesIds.get(name);
    lock.get(variableId).lock();
    if (!senderId.equals(lockHolders.get(variableId))) {
      lock.get(variableId).unlock();
      return false;
    }
    for (UUID nodeId : clusters.get(variableId)) {
      if (nodeId != senderId) {
        nodeQueues.get(nodeId).get(variableId).addLast(new SimpleEntryComparable<>(senderId, variable));
      }
    }
    lock.get(variableId).unlock();
    return true;
  }


  /**
   * read the value of a DSM variable.
   *
   * @param nodeId the reader node ID
   * @param name name of the variable
   * @return value if there is a value for the given name null otherwise.
   */
  public AbstractMap.SimpleEntry<UUID, Object> read(UUID nodeId, String name) throws NullPointerException {
    if (!variablesIds.containsKey(name)) {
      Simulator.getLogger().error("[SharedVariable] Read: no variable with name " + name + " is registered");
      new ClassNotFoundException("[SharedVariable] Read: no variable with name " + name + " is registered");
    }
    int variableId = variablesIds.get(name);

    if (!nodeQueues.get(nodeId).containsKey(variableId)) {
      Simulator.getLogger().error("[SharedVariable] Read: the node with ID " + nodeId + " does not have "
            + "access to the variable with name " + name);
      throw new NullPointerException("[SharedVariable] Read: the node with ID " + nodeId + " does not have "
            + "access to the variable with name " + name);
    }
    if (nodeQueues.get(nodeId).get(variableId).isEmpty()) {
      Simulator.getLogger().debug("[SharedVariable] Read: no present values for variable with name " + name);
      return null;
    }
    return nodeQueues.get(nodeId).get(variableId).poll();
  }

  /**
   * get the owner of the lock for a specific variable.
   *
   * @param name name of the variable
   * @return owner of the variable
   */
  public UUID getOwner(String name) {
    int variableId = variablesIds.get(name);
    lock.get(variableId).lock();
    UUID owner = lockHolders.get(variableId);
    lock.get(variableId).unlock();
    return owner;
  }

  /**
   * Checks whether there is a value with that variable name or not.
   *
   * @param nodeId Id of the node
   * @param name name of the variable
   * @return true if empty false otherwise
   */
  public boolean isEmpty(UUID nodeId, String name) {
    if (!variablesIds.containsKey(name)) {
      Simulator.getLogger().error("[SharedVariable] Read: no variable with name " + name + " is registered");
      new ClassNotFoundException("[SharedVariable] Read: no variable with name " + name + " is registered");
    }
    int variableId = variablesIds.get(name);
    if (!nodeQueues.get(nodeId).containsKey(variableId)) {
      Simulator.getLogger().error("[SharedVariable] Read: the node with ID " + nodeId + " does not have "
            + "access to the variable with name " + name);
      throw new NullPointerException("[SharedVariable] Read: the node with ID " + nodeId + " does not have "
            + "access to the variable with name " + name);
    }
    return nodeQueues.get(nodeId).get(variableId).isEmpty();
  }

  /**
   * releases lock for given name and ID.
   *
   * @param nodeId Id of the node
   * @param name name of the variable
   */
  public void releaseLock(UUID nodeId, String name) {
    if (getOwner(name).equals(nodeId)) {
      int variableId = variablesIds.get(name);
      lockHolders.set(variableId, fixtureOwner);
    }
  }
}
