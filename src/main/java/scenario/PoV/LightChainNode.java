package scenario.PoV;

import Metrics.SimulatorGauge;
import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;
import scenario.PoV.events.*;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LightChainNode implements BaseNode {

  final int transactionInsertions = 100;
  final int blockIterations = 50;
  final int numValidators = 1;
  final int txMin = 1;
  final int transactionInsertionDelay = 1000; // (ms)
  final int blockInsertionDelay = 2000; // (ms)
  final int updateWaitTime = 500; // (ms)
  /**
   * A LightChainNode represents a single node of the LightChain protocol. Given the limitations of the Simulator, for
   * this implementation we define two types of LightChain Nodes. A regular LightChainNode, and a registry node.
   * A regular LightChain node performs the typical LightChain protocol. Whereas the registry node represents the
   * underlying skip graph overlay, where it keeps the list of inserted transactions and blocks, and receive requests
   * from different nodes to submit and retrieve transactions and blocks. This means that this class contains attributes
   * and functions for two different types of node. This division is a result of the fact that the simulator design can
   * handle only a single node type. Perhaps it would be an interesting idea to extend it later to support more than
   * one type of nodes.
   */

  private List<UUID> allID;
  private UUID uuid;
  private MiddleLayer network;
  private Logger logger;
  private Map<UUID, Transaction> transactions;
  private Map<UUID, Block> blocks;
  private Map<UUID, Integer> transactionValidationCount;
  private Map<UUID, Integer> blockValidationCount;
  private Block latestBlock;
  private List<Transaction> requestedTransactions;
  private CountDownLatch blockLatch;
  private CountDownLatch transactionLatch;
  private Integer maximumHeight;
  private Integer totalTransactionCount;
  private ReadWriteLock transactionValidationLock;
  private ReadWriteLock blockValidationLock;


  /**
   * Constructor of LightChain Node
   *
   * @param uuid    ID of the node
   * @param network used to communicate with other nodes
   */
  public LightChainNode(UUID uuid, MiddleLayer network) {
    this.uuid = uuid;
    this.network = network;
    this.transactions = new HashMap<>();
    this.blocks = new HashMap<>();
    this.transactionValidationCount = new HashMap<>();
    this.blockValidationCount = new HashMap<>();
    this.transactionValidationLock = new ReentrantReadWriteLock();
    this.blockValidationLock = new ReentrantReadWriteLock();
    this.logger = Logger.getLogger(LightChainNode.class.getName());

  }

  public LightChainNode() {

  }

  /**
   * On the creation of a LightChain node, first the node checks if it is a registry node or not. A registry node
   * is the node of UUID placed in index 0 of allID list. This convention is pre-defined for LightChainNode. So the
   * node checks in the beginning if its UUID matches the UUID of the 0-index element of allID. If it matches then it
   * sets isRegistry variable to true, or false otherwise. If the node is the registry node, the it appends the genesis
   * block to its the list of blocks.
   *
   * @param allID the IDs of type UUID for all the nodes in the cluster
   */
  @Override
  public void onCreate(ArrayList<UUID> allID) {

    logger.info("Node " + this.uuid + " has been created.");

    this.allID = allID;

    // ensure that number of validators is small than number of nodes
    if (numValidators > this.allID.size() - 1) try {
      throw new Exception("Number of validators must be smaller than number of nodes. NumValidators= " + numValidators + ", numNodes= " + (this.allID.size() - 1));
    } catch (Exception e) {
      e.printStackTrace();
    }

    network.ready();
  }

  /**
   * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {
    logger.info("Node " + this.uuid + " has started.");

    new Thread(() -> {
      startTransactionInsertions();
    }).start();

    new Thread(() -> {
      startBlockInsertion();
    }).start();
  }

  /**
   * Stops the LightChain Node
   */
  @Override
  public void onStop() {

  }

  /**
   * Performs the action of the message by passing an instance of this LightChain Node
   *
   * @param originID the ID of the sender node
   * @param msg      the content of the message
   */
  @Override
  public void onNewMessage(UUID originID, Event msg) {
    msg.actionPerformed(this);
  }

  /**
   * @param selfID  the ID of the new node
   * @param network communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(UUID selfID, MiddleLayer network) {
    return new LightChainNode(selfID, network);
  }

  /**
   * This function is invoked on the start of the node and it attempts to iterate and insert the required number of
   * transactions into the network. First, the node request an update of its view of the latest block Then, it gets the
   * validators of the transaction, the it asynchronously asks the validators to validate the transactions. Once the
   * transaction is validated by all validators, this will be detected at the ConfirmTransactionValidation function
   * above and that function will take care of inserting the transaction.
   * <p>
   * TODO: This function is supposed to run either on a differen thread from the block insertion, or incorporate the
   * block insertion within it somehow
   */
  public void startTransactionInsertions() {

    logger.info("Transaction insertion for node " + this.uuid + "started");

    for (int i = 0; i < this.transactionInsertions; ++i) {

      logger.info("Node " + this.uuid + " inserting transaction number " + (i + 1));

      // update the latest block
      this.requestLatestBlock();

      logger.info("Latest block is updated for node " + this.uuid);

      // get the validators
      List<UUID> validators = this.getValidators();
      // create the transaction
      Transaction tx = new Transaction(UUID.randomUUID(), this.uuid, this.latestBlock, validators);
      // initialize the counter of the transaction and store the transaction for insertion later
      this.transactionValidationCount.put(tx.getID(), 0);
      this.transactions.put(tx.getID(), tx);


      logger.info("Node " + this.uuid + " is requesting validators");
      for (UUID validator : validators) {
        // send an asynchronous validation request
        network.send(validator, new ValidateTransactionEvent(tx));
      }

      // wait for some time in between insertions
      try {
        Thread.sleep(this.transactionInsertionDelay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    int count = 0;
    for(UUID key : this.transactionValidationCount.keySet()) {

      if(this.transactionValidationCount.get(key) != this.numValidators)
        count += 1;
    }

    logger.info("Reporting from node " + this.uuid + " " + count + " un-fully validated transactions");
  }

  /**
   * This function handles the collection of transactions and casting them into blocks and then inserting these blocks
   * to the registry
   */
  public void startBlockInsertion() {

    for (int i = 0; i < this.blockIterations; ++i) {

      try {
        Thread.sleep(this.blockInsertionDelay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      logger.info("Block collection attempt " + (i + 1) + " for node " + this.uuid);

      this.requestTransactions();
      this.requestLatestBlock();

      List<Transaction> collectedTransaction = this.requestedTransactions;

      List<UUID> transactionIDs = new ArrayList<>();
      for (Transaction tx : collectedTransaction)
        transactionIDs.add(tx.getID());

      if (collectedTransaction.isEmpty()) {
        logger.info("Transaction collection Attempt" + (i + 1) + " failed for node " + this.uuid);
        continue;
      }

      logger.info("Getting Block validators for node " + this.uuid);
      List<UUID> validators = getValidators();

      Block block = new Block(UUID.randomUUID(), this.latestBlock.getHeight() + 1, this.uuid, this.latestBlock.getID(), validators, transactionIDs);

      this.blockValidationCount.put(block.getID(), 0);
      this.blocks.put(block.getID(), block);

      logger.info("Requesting block validations at node " + this.uuid);
      for (UUID validator : validators) {
        // send an asynchronous validation request
        network.send(validator, new ValidateBlockEvent(block));
      }
    }
  }

  /**
   * This function is invoked when a validator wants to confirm their validation of a transaction. The LightChain node
   * maintains a map with a counter for every transaction it creates. This function receives the UUID of the transaction
   * that is being verified and it increases its counter. Once the counter of a transaction is equal to the number of
   * validators, this means the transaction has been validated and is ready to be inserted. So this function attempts
   * to insert the transaction into the network by sending a submit transaction event to the registry node.
   *
   * @param transactionUUID
   */
  public void confirmTransactionValidation(UUID transactionUUID) {

    if (!transactionValidationCount.containsKey(transactionUUID)) try {
      throw new Exception("Confirming a non-existing transaction");
    } catch (Exception e) {
      e.printStackTrace();
    }

    logger.info("Transaction " + transactionUUID + " owned by " + this.uuid + " received a confirmation");

    this.transactionValidationLock.writeLock().lock();
    Integer prevCount = transactionValidationCount.get(transactionUUID);
    transactionValidationCount.put(transactionUUID, prevCount + 1);
    this.transactionValidationLock.writeLock().unlock();

    if (prevCount + 1 == this.numValidators) {
      logger.info("Node " + this.uuid + " Inserting its transaction " + transactionUUID);
      this.network.send(this.getRegistryID(), new SubmitTransactionEvent(this.transactions.get(transactionUUID)));
    }
  }

  public void confirmBlockValidation(UUID blockUUID) {

    if (!blockValidationCount.containsKey(blockUUID)) try {
      throw new Exception("Confirming a non-existing transaction");
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.blockValidationLock.writeLock().lock();
    Integer prevCount = blockValidationCount.get(blockUUID);
    blockValidationCount.put(blockUUID, prevCount + 1);
    this.blockValidationLock.writeLock().unlock();

    if (prevCount + 1 == this.numValidators) {
      logger.info("Node " + this.uuid + " Inserting its block " + blockUUID);
      this.network.send(this.getRegistryID(), new SubmitBlockEvent(this.blocks.get(blockUUID)));
    }
  }

  /**
   * @return the UUID of the registry node
   */
  public UUID getRegistryID() {
    return this.allID.get(0);
  }


  /**
   * This function is invokes when another node requests a validation from this node. It essentially accepts
   * the validation without any conditions and immediately replies with its confirmation
   * f* TODO: with is algorithm, a node can be chosen to be its own validator, fix this to prevent this case.
   *
   * @param transaction
   */
  public void validateTransaction(Transaction transaction) {

    network.send(transaction.getOwner(), new ConfirmTransactionEvent(transaction.getID()));
  }

  public void validateBlock(Block block) {

    network.send(block.getOwner(), new ConfirmBlockEvent(block.getID()));
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
    for (int i = this.numValidators + 1; i < this.allID.size(); ++i) {
      int j = rand.nextInt(i + 1);
      if (j < this.numValidators) randomIndexes.set(j, i);
    }

    List<UUID> validators = new ArrayList<>();
    for (Integer index : randomIndexes) {
      validators.add(this.allID.get(index));
    }

    return validators;
  }

  /**
   * @return true if this node is a registry node, false otherwise
   */
  public boolean isRegistry() {
    return false;
  }

  /**
   * this function is called by the registry node through an event in order to supply this node with the latest block
   * upon its an asynchronous request that was carried out earlier.
   *
   * @param block
   */
  public void updateLatestBlock(Block block) {
    logger.info("Latest Block " + block.getID() + " updated for node " + this.uuid);
    this.latestBlock = block;
    this.blockLatch.countDown();
  }

  /**
   * This function causes the thread to sleep for a fixed time between sending a request and awaiting its response in
   * order to give time for the response to arrive.
   */
  public void updateWait() {
    try {
      Thread.sleep(this.updateWaitTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * This function is called by this node to request the latest block from the registry node asynchronously, the it
   * sleeps the thread for a while to give time for the registry node to send the latest block. This is necessary given
   * that the simulator only support asynchronous events.
   */
  public void requestLatestBlock() {

    logger.info("Node " + this.uuid + " requesting latest block");

    blockLatch = new CountDownLatch(1);
    network.send(this.getRegistryID(), new GetLatestBlockEvent(this.uuid));

    logger.info("Node" + this.uuid + " waiting for latest block");

    try {
      blockLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("Node + " + this.uuid + ": latest block received");
  }

  /**
   * This function is called to request a collection of transactions from the registry node.
   */
  public void requestTransactions() {

    this.transactionLatch = new CountDownLatch(1);

    network.send(this.getRegistryID(), new CollectTransactionsEvent(this.uuid, this.txMin));

    try {
      transactionLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  public void deliverTransactions(List<Transaction> requestedTransactions) {
    logger.info("Requested Transactions received by node " + this.uuid);
    this.requestedTransactions = requestedTransactions;
    this.transactionLatch.countDown();
  }


  /**
   * This function runs on a separate thread and records the maximum block height every second
   */
  public void monitorBlockHeight() {

    while (true) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      SimulatorGauge.set("block_height_per_time", this.uuid, this.maximumHeight);
    }
  }

}
