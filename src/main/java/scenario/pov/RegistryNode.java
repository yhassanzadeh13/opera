package scenario.pov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import metrics.SimulatorGauge;
import metrics.SimulatorHistogram;
import node.BaseNode;
import org.apache.log4j.Logger;
import scenario.pov.events.DeliverLatestBlockEvent;
import scenario.pov.events.DeliverTransactionsEvent;
import underlay.MiddleLayer;
import underlay.packets.Event;

/**
 * The registry node represents the underlying skip graph overlay of the RegistryNode, where it keeps the
 * list of inserted transactions and blocks, and receive requests
 * from different nodes to submit and retrieve transactions and blocks.
 */
public class RegistryNode implements BaseNode {

  final int blockIterations = 50;
  final int numValidators = 1;
  private List<UUID> allId;
  private UUID uuid;
  private MiddleLayer network;
  private Logger logger;
  private Integer maximumHeight;
  private Integer totalTransactionCount;
  // only for registry node
  private List<Transaction> availableTransactions;
  private List<Block> insertedBlocks;
  private Map<Integer, Integer> heightToUniquePrevCount;
  private Map<Integer, Map<UUID, Integer>> heightToUniquePrev;
  private SimulatorGauge gauge;
  private SimulatorHistogram histogram;

  /**
   * Constructor of Registry Node.
   *
   * @param uuid ID of the node
   * @param network used to communicate with other nodes
   */
  public RegistryNode(UUID uuid, MiddleLayer network) {
    this.uuid = uuid;
    this.network = network;
    this.logger = Logger.getLogger(LightChainNode.class.getName());

    // for registry nodes
    this.availableTransactions = new ArrayList<>();
    this.insertedBlocks = new ArrayList<>();
    this.heightToUniquePrev = new HashMap<>();
    this.heightToUniquePrevCount = new HashMap<>();
    this.maximumHeight = 0;
    this.totalTransactionCount = 0;
  }

  public RegistryNode() {

  }

  /**
   * On the creation of a LightChain node, first the node checks if it is a registry node or not. A registry node
   * is the node of UUID placed in index 0 of allId list. This convention is pre-defined for LightChainNode. So the
   * node checks in the beginning if its UUID matches the UUID of the 0-index element of allId. If it matches then it
   * sets isRegistry variable to true, or false otherwise. If the node is the registry node, the it appends the genesis
   * block to its the list of blocks.
   *
   * @param allId the IDs of type UUID for all the nodes in the cluster
   */
  @Override
  public void onCreate(ArrayList<UUID> allId) {

    logger.info("Node " + this.uuid + " has been created.");

    this.allId = allId;

    // ensure that number of validators is small than number of nodes
    if (numValidators > this.allId.size() - 1) {
      try {
        throw new Exception("Number of validators must be smaller than number of nodes. NumValidators= "
              + numValidators + ", numNodes= " + (this.allId.size() - 1));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    double[] linespace = new double[this.allId.size() * this.blockIterations];

    for (int i = 0; i < linespace.length; i++) {
      linespace[i] = i;
    }

    gauge.register("transaction_count");
    gauge.register("block_height_per_time");
    histogram.register("block_height_histogram", linespace);
    histogram.register("unique_blocks_per_height", linespace);

    new Thread(() -> {
      monitorBlockHeight();
    }).start();

    logger.info("[Registry] The Registry node is " + this.uuid);
    this.appendBlock(new Block(UUID.randomUUID(), 0, this.uuid, UUID.randomUUID(),
          new ArrayList<>(), new ArrayList<>()));
    logger.info("[Registry] Genesis Block has been appended");


    network.ready();
  }

  /**
   * starts the Registry Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {
  }

  /**
   * Stops the Registry Node.
   */
  @Override
  public void onStop() {

  }

  /**
   * Performs the action of the message by passing an instance of this Registry Node.
   *
   * @param originId the ID of the sender node
   * @param msg      the content of the message
   */
  @Override
  public void onNewMessage(UUID originId, Event msg) {
    msg.actionPerformed(this);
  }

  /**
   * Returns a new registry node with given UUID and network.
   *
   * @param selfId  the ID of the new node
   * @param network communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(UUID selfId, MiddleLayer network) {
    return new RegistryNode(selfId, network);
  }


  /**
   * This function randomly chooses validators of a certain transaction using the Reservoir Sampling Algorithm
   * see https://www.geeksforgeeks.org/reservoir-sampling/
   *
   * @return a list of UUID's of randomly chosen nodes from the network
   */
  public List<UUID> getValidators() {

    logger.info("Fetching validators for node " + this.uuid);

    // add the first numValidators nodes
    List<Integer> randomIndexes = new ArrayList<>();
    for (int i = 1; i <= this.numValidators; ++i) {
      randomIndexes.add(i);
    }

    Random rand = new Random();
    for (int i = this.numValidators + 1; i < this.allId.size(); ++i) {
      int j = rand.nextInt(i + 1);
      if (j < this.numValidators) {
        randomIndexes.set(j, i);
      }
    }

    List<UUID> validators = new ArrayList<>();
    for (Integer index : randomIndexes) {
      validators.add(this.allId.get(index));
    }

    return validators;
  }

  /**
   * Checks whether a node is registry or not.
   *
   * @return true if this node is a registry node, false otherwise
   */
  public boolean isRegistry() {
    return true;
  }

  /*
  These functions below belong solely to the registry node.
   */

  /**
   * This function is invoked from a node to insert a transaction after it has been validated.
   * It simply appends the transaction to the list of available transactions for collection. It also provides locking
   * to ensure the correctness of adding and removing transactions.
   *
   * @param transaction transaction to be inserted into the network
   */
  public void addTransaction(Transaction transaction) {

    gauge.inc("transaction_count", this.uuid);

    logger.info("[Registry] new transaction inserted into network.");

    //  this.transactionLock.writeLock().lock();

    this.availableTransactions.add(transaction);
    this.totalTransactionCount += 1;
    logger.info("[Registry] currently " + this.availableTransactions.size() + " transactions are available");
    logger.info("[Registry] total number of transactions inserted so far " + this.totalTransactionCount);

    //  this.transactionLock.writeLock().unlock();
  }

  /**
   * This function is called from a node that is attempting to collect transaction to create a block. It takes a set
   * of transactions and returns a list of then.
   *
   * @param requester      ID of node requesting transactions
   * @param requiredNumber the required number of transactions
   * @return a list of transactions matching the number required
   */
  public List<Transaction> collectTransactions(UUID requester, Integer requiredNumber) {
    //   this.transactionLock.writeLock().lock();

    List<Transaction> requestedTransactions = new ArrayList<>();
    // a failed collection attempts
    if (this.availableTransactions.size() < requiredNumber) {

      logger.info("[Registry] number of available transactions is less than requested by node "
            + requester + ", required number: " + requiredNumber + ", available number: "
            + this.availableTransactions.size());

      //    this.transactionLock.writeLock().unlock();

      network.send(requester, new DeliverTransactionsEvent(requestedTransactions));

      return requestedTransactions;
    }

    for (int i = 0; i < this.availableTransactions.size(); ++i) {
      requestedTransactions.add(this.availableTransactions.get(i));
    }

    List<Transaction> temporary = new ArrayList<>();
    for (int i = requiredNumber; i < this.availableTransactions.size(); ++i) {
      temporary.add(this.availableTransactions.get(i));
    }
    this.availableTransactions = temporary;

    //  this.transactionLock.writeLock().unlock();

    gauge.dec("transaction_count", this.uuid, requiredNumber);

    network.send(requester, new DeliverTransactionsEvent(requestedTransactions));

    return null;
  }

  /**
   * This functios is invoked by a node that is inserting a block after it has been validated to the registry. It also
   * provides a lock to ensure the correctness of the write operations on the ledger.
   *
   * @param block block to be appended to the ledger
   */
  public void appendBlock(Block block) {

    logger.info("[Registry] New Block appended to Ledger");
    //  this.blockLock.writeLock().lock();

    this.insertedBlocks.add(block);
    this.maximumHeight = Math.max(this.maximumHeight, block.getHeight());

    if (!this.heightToUniquePrev.containsKey(block.getHeight())) {
      this.heightToUniquePrev.put(block.getHeight(), new HashMap<>());
    }
    if (!this.heightToUniquePrev.get(block.getHeight()).containsKey(block.getPrev())) {
      Integer oldValue = this.heightToUniquePrevCount.get(block.getHeight());
      if (oldValue == null) {
        oldValue = 0;
      }
      this.heightToUniquePrevCount.put(block.getHeight(), oldValue + 1);

      histogram.observe("unique_blocks_per_height", this.uuid, block.getHeight());
    }

    Integer old = this.heightToUniquePrev.get(block.getHeight()).get(block.getPrev());
    if (old == null) {
      old = 0;
    }
    this.heightToUniquePrev.get(block.getHeight()).put(block.getPrev(), old + 1);


    histogram.observe("block_height_histogram", this.uuid, block.getHeight());

    logger.info("[Registry] maximum height found so far is " + this.maximumHeight);
    logger.info("[Registry] currently " + this.insertedBlocks.size() + " blocks are inserted totally");

    // this.blockLock.writeLock().unlock();
  }

  /**
   * This function is invoked as a result of a node requesting the latest block from the registry.
   *
   * @param requester of the node requesting the latest block so that its request can be delivered
   * @return the latest block on the ledger
   */
  public Block getLatestBlock(UUID requester) {

    logger.info("[Registry] Getting Latest Block for node " + requester);

    Block latestBlock;

    // this.blockLock.readLock().lock();
    latestBlock = this.insertedBlocks.get(this.insertedBlocks.size() - 1);
    long hash = latestBlock.getId().hashCode();
    int height = latestBlock.getHeight();
    Block chosenBlock = latestBlock;
    for (int i = this.insertedBlocks.size() - 1; i >= 0; --i) {

      if (this.insertedBlocks.get(i).getHeight() != height) {
        break;
      }
      long blockHash = this.insertedBlocks.get(i).getId().hashCode();

      if (blockHash < hash) {
        hash = blockHash;
        chosenBlock = this.insertedBlocks.get(i);
      }
    }
    logger.info("[Registry] " + this.insertedBlocks.size() + " blocks found");

    // this.blockLock.readLock().unlock();

    logger.info("[Registry] Sending Latest Block " + latestBlock.getId() + " to node " + requester);
    this.network.send(requester, new DeliverLatestBlockEvent(latestBlock));

    return chosenBlock;
  }

  /**
   * This function runs on a separate thread and records the maximum block height every second.
   */
  public void monitorBlockHeight() {

    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      gauge.set("block_height_per_time", this.uuid, this.maximumHeight);
    }
  }

}