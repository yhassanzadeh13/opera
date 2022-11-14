package utils;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import node.Identifier;
import node.IdentifierGenerator;
import simulator.Simulator;

/**
 * local static variable between the nodes with a buffer size of 1
 * (any two consecutive writing on the same variable is forbidden).
 */
public class SharedVariable {
  private final Logger logger = OperaLogger.getLoggerForSimulator(SharedVariable.class.getCanonicalName());
  // singleton instance
  private static SharedVariable instance = null;
  private final ConcurrentHashMap<Identifier, ConcurrentHashMap<Integer, ArrayDeque<SimpleEntryComparable<Identifier, Object>>>> nodeQueues;
  // for each variable ID, hold the cluster of that variable
  private final ArrayList<ArrayList<Identifier>> clusters;
  // for each new variable, assign a new id for it
  private final ConcurrentHashMap<String, Integer> variablesIds;
  // for each variable, keep the lock holder
  private final ArrayList<Identifier> lockHolders;
  // for every variable, hold a latch that tell us if a node is currently writing
  private final ArrayList<ReentrantLock> lock;
  private final Identifier fixtureOwner;

  private SharedVariable() {
    nodeQueues = new ConcurrentHashMap<>();
    clusters = new ArrayList<>();
    variablesIds = new ConcurrentHashMap<>();
    lockHolders = new ArrayList<>();
    lock = new ArrayList<>();
    fixtureOwner = IdentifierGenerator.newIdentifier();

  }

  /**
   * Getter of instance of shared variable.
   *
   * @return the instance of the shared variable
   */
  @SuppressFBWarnings(value = {"MS_EXPOSE_REP", "LI_LAZY_INIT_STATIC"})
  public static SharedVariable getInstance() {
    if (SharedVariable.instance == null) {
      instance = new SharedVariable();
    }
    return SharedVariable.instance;
  }

  /**
   * Register a new variable in the DSM.
   *
   * @param name  name of the variable
   * @param allId the IDs of the nodes that should have access to this variable
   * @return false if variable already registered, true otherwise
   */
  public boolean register(String name, ArrayList<Identifier> allId) {
    if (variablesIds.containsKey(name)) {
      // TODO: throw illegal state exception
      return false;
    }
    // add cluster and assign a new ID
    int variableId = clusters.size();
    variablesIds.put(name, variableId);
    clusters.add(allId);

    // add new queue for all nodes
    for (Identifier nodeId : allId) {
      nodeQueues.putIfAbsent(nodeId, new ConcurrentHashMap<>());
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
   * @param nodeId identifier of the node.
   * @param name   name of the variable.
   * @return true if node equal to fixture owner or nodeID.
   */
  public synchronized boolean requestLock(Identifier nodeId, String name) {
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
   * @param senderId node Identifier.
   * @param name     name of the value
   * @param variable variable to overwrite
   * @return Ture in case of success, False otherwise.
   */
  public synchronized boolean write(Identifier senderId, String name, Object variable) {
    if (!variablesIds.containsKey(name)) {
      // TODO: throw illegal state exception
      return false;
    }
    int variableId = variablesIds.get(name);
    lock.get(variableId).lock();
    if (!senderId.equals(lockHolders.get(variableId))) {
      lock.get(variableId).unlock();
      return false;
    }
    for (Identifier nodeId : clusters.get(variableId)) {
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
   * @param nodeId the reader node identifier.
   * @param name   name of the variable
   * @return value if there is a value for the given name null otherwise.
   */
  public AbstractMap.SimpleEntry<Identifier, Object> read(Identifier nodeId, String name) throws IllegalArgumentException {
    if (!variablesIds.containsKey(name)) {
      throw new IllegalArgumentException("no variable with name " + name + " is registered to read on shared variable");
    }

    int variableId = variablesIds.get(name);
    if (!nodeQueues.get(nodeId).containsKey(variableId)) {
      throw new IllegalArgumentException("node identifier: " + nodeId + " does not have access to the variable with name: " + name);
    }
    if (nodeQueues.get(nodeId).get(variableId).isEmpty()) {
      logger.warn("node identifier {} does not have any value for the variable with name {}", nodeId, name);
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
  public Identifier getOwner(String name) {
    int variableId = variablesIds.get(name);
    lock.get(variableId).lock();
    Identifier owner = lockHolders.get(variableId);
    lock.get(variableId).unlock();
    return owner;
  }

  /**
   * Checks whether there is a value with that variable name or not.
   *
   * @param nodeId identifier of the node.
   * @param name   name of the variable
   * @return true if empty false otherwise
   */
  public boolean isEmpty(Identifier nodeId, String name) throws IllegalArgumentException {
    if (!variablesIds.containsKey(name)) {
      throw new IllegalArgumentException("no variable with name " + name + " is registered");
    }
    int variableId = variablesIds.get(name);
    if (!nodeQueues.get(nodeId).containsKey(variableId)) {
      throw new IllegalArgumentException("node: " + nodeId + " does not have access to the variable with name: " + name);
    }
    return nodeQueues.get(nodeId).get(variableId).isEmpty();
  }

  /**
   * releases lock for given name and identifier.
   *
   * @param nodeId identifier of the node.
   * @param name   name of the variable.
   */
  public void releaseLock(Identifier nodeId, String name) {
    if (getOwner(name).equals(nodeId)) {
      int variableId = variablesIds.get(name);
      lockHolders.set(variableId, fixtureOwner);
    }
  }
}
