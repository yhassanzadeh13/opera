package scenario.pov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import metrics.MetricsCollector;
import node.BaseNode;
import org.apache.log4j.Logger;
import scenario.pov.events.CollectTransactionsEvent;
import scenario.pov.events.ConfirmBlockEvent;
import scenario.pov.events.ConfirmTransactionEvent;
import scenario.pov.events.GetLatestBlockEvent;
import scenario.pov.events.SubmitBlockEvent;
import scenario.pov.events.SubmitTransactionEvent;
import scenario.pov.events.ValidateBlockEvent;
import scenario.pov.events.ValidateTransactionEvent;
import underlay.MiddleLayer;
import underlay.packets.Event;


/**
 * A LightChainNode represents a single node of the LightChain protocol.
 * Given the limitations of the simulator this implementation defines two types of LightChain Nodes.
 * A regular LightChainNode, and a registry node.
 * A regular LightChain node performs the typical LightChain protocol.
 * Whereas the registry node represents the underlying skip graph overlay,
 * where it keeps the list of inserted transactions and blocks,
 * and receive requests from different nodes to submit and retrieve transactions and blocks.
 * This means that this class contains attributes
 * and functions for two different types of node.
 * This division is result of the fact that the simulator design can handle only a single node type.
 * Perhaps it would be an interesting idea to extend it later to support more than
 * one type of nodes.
 */
public class LightChainNode implements BaseNode {

  final int transactionInsertions = 100;
  final int blockIterations = 50;
  final int numValidators = 1;
  final int txMin = 1;
  final int transactionInsertionDelay = 1000; // (ms)
  final int blockInsertionDelay = 2000; // (ms)
  final int updateWaitTime = 500; // (ms)

  private List<UUID> allId;
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
  private MetricsCollector metricsCollector;
  private ReadWriteLock transactionValidationLock;
  private ReadWriteLock blockValidationLock;

  /**
   * Constructor of LightChain Node.
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
   * On the creation of a LightChain node, first the node checks if it is a registry node or not.
   * A registry node is the node of UUID placed in index 0 of allID list.
   * This convention is pre-defined for LightChainNode.
   * So the node checks if its UUID matches the UUID of the 0-index element of allID.
   * If it matches then it sets isRegistry variable to true, or false otherwise.
   * If the node is the registry node, then it appends the genesis block to its list of blocks.
   *
   * @param allId the IDs of type UUID for all the nodes in the cluster
   */
  @Override
  public void onCreate(ArrayList<UUID> allId) {

    logger.info("node " + this.uuid + " has been created.");

    this.allId = allId;

    // ensure that number of validators is small than number of nodes
    if (numValidators > this.allId.size() - 1) {
      try {
        throw new Exception(
              "Number of validators must be smaller than number of nodes. NumValidators= "
                    + numValidators + ", numNodes= " + (this.allId.size() - 1));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    network.ready();
  }

  /**
   * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {

    logger.info("node " + this.uuid + " has started.");

    new Thread(this::startTransactionInsertions).start();

    new Thread(this::startBlockInsertion).start();
  }

  /**
   * Stops the LightChain Node.
   */
  @Override
  public void onStop() {

  }

  /**
   * Performs the action of the message by passing an instance of this LightChain Node.
   *
   * @param originId the ID of the sender node
   * @param msg      the content of the message
   */
  @Override
  public void onNewMessage(UUID originId, Event msg) {
    msg.actionPerformed(this);
  }

  /**
   * creates a new instance of LightChainNode.
   *
   * @param selfId  the ID of the new node
   * @param network communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(UUID selfId, MiddleLayer network) {
    return new LightChainNode(selfId, network);
  }

  /**
   * Function attempts to iterate and insert the required number of transactions into the network.
   * First, the node request an update of its view of the latest block.
   * Then, it gets the validators of the transaction.
   * Then it asynchronously asks the validators to validate the transactions.
   * Once the transaction is validated by all validators, ConfirmTransactionValidation detects it
   * above and that function will take care of inserting the transaction.
   *
   *
   * <p>TODO: This function is supposed to run either on a different thread from the block insertion, or incorporate the
   * block insertion within it somehow
   */
  public void startTransactionInsertions() {

    logger.info("Transaction insertion for node " + this.uuid + "started");

    for (int i = 0; i < this.transactionInsertions; ++i) {

      logger.info("node " + this.uuid + " inserting transaction number " + (i + 1));

      // update the latest block
      this.requestLatestBlock();

      logger.info("Latest block is updated for node " + this.uuid);

      // get the validators
      List<UUID> validators = this.getValidators();
      // create the transaction
      Transaction tx = new Transaction(UUID.randomUUID(), this.uuid, this.latestBlock, validators);
      // initialize the counter of the transaction and store the transaction for insertion later
      this.transactionValidationCount.put(tx.getId(), 0);
      this.transactions.put(tx.getId(), tx);


      logger.info("node " + this.uuid + " is requesting validators");
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
    for (UUID key : this.transactionValidationCount.keySet()) {

      if (this.transactionValidationCount.get(key) != this.numValidators) {
        count += 1;
      }
    }

    logger.info("Reporting from node " + this.uuid + " " + count + " un-fully validated transactions");
  }

  /**
   * This function handles the collection of transactions and casting them into blocks and then inserting these blocks.
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

      List<UUID> transactionIds = new ArrayList<>();
      for (Transaction tx : collectedTransaction) {
        transactionIds.add(tx.getId());
      }

      if (collectedTransaction.isEmpty()) {
        logger.info("Transaction collection Attempt" + (i + 1) + " failed for node " + this.uuid);
        continue;
      }

      logger.info("Getting Block validators for node " + this.uuid);
      List<UUID> validators = getValidators();

      Block block = new Block(UUID.randomUUID(),
            this.latestBlock.getHeight() + 1,
            this.uuid, this.latestBlock.getId(),
            validators,
            transactionIds);

      this.blockValidationCount.put(block.getId(), 0);
      this.blocks.put(block.getId(), block);

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
   * that is being verified, and it increases its counter. Once the counter of a transaction is equal to the number of
   * validators, this means the transaction has been validated and is ready to be inserted. So this function attempts
   * to insert the transaction into the network by sending a submit transaction event to the registry node.
   *
   * @param transactionUuid unique Id of the trasaction.
   */
  public void confirmTransactionValidation(UUID transactionUuid) {

    if (!transactionValidationCount.containsKey(transactionUuid)) {
      try {
        throw new Exception("Confirming a non-existing transaction");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    logger.info("Transaction " + transactionUuid + " owned by " + this.uuid + " received a confirmation");

    this.transactionValidationLock.writeLock().lock();
    Integer prevCount = transactionValidationCount.get(transactionUuid);
    transactionValidationCount.put(transactionUuid, prevCount + 1);
    this.transactionValidationLock.writeLock().unlock();

    if (prevCount + 1 == this.numValidators) {
      logger.info("node " + this.uuid + " Inserting its transaction " + transactionUuid);
      this.network.send(this.getRegistryId(), new SubmitTransactionEvent(this.transactions.get(transactionUuid)));
    }
  }

  /** getter of registery node's ID.
   *
   * @return the UUID of the registry node.
   */
  public UUID getRegistryId() {
    return this.allId.get(0);
  }

  /**
   * Confirms validation of the block with its UUID.
   *
   * @param blockUuid UUID of the block.
   */
  public void confirmBlockValidation(UUID blockUuid) {

    if (!blockValidationCount.containsKey(blockUuid)) {
      try {
        throw new Exception("Confirming a non-existing transaction");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.blockValidationLock.writeLock().lock();
    Integer prevCount = blockValidationCount.get(blockUuid);
    blockValidationCount.put(blockUuid, prevCount + 1);
    this.blockValidationLock.writeLock().unlock();

    if (prevCount + 1 == this.numValidators) {
      logger.info("node " + this.uuid + " Inserting its block " + blockUuid);
      this.network.send(this.getRegistryId(), new SubmitBlockEvent(this.blocks.get(blockUuid)));
    }
  }

  /**
   * This function is invokes when another node requests a validation from this node. It essentially accepts
   * the validation without any conditions and immediately replies with its confirmation
   * f* TODO: with is algorithm, a node can be chosen to be its own validator, fix this to prevent this case.
   *
   * @param transaction a transaction object to validate.
   */
  public void validateTransaction(Transaction transaction) {

    network.send(transaction.getOwner(), new ConfirmTransactionEvent(transaction.getId()));
  }

  public void validateBlock(Block block) {

    network.send(block.getOwner(), new ConfirmBlockEvent(block.getId()));
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
   * Checker whether the node is a registry node or not.
   *
   * @return true if this node is a registry node, false otherwise
   */
  public boolean isRegistry() {
    return false;
  }

  /**
   * this function is called by the registry node through an event in order to supply this node with the latest block
   * upon its asynchronous request that was carried out earlier.
   *
   * @param block block to update the node
   */
  public void updateLatestBlock(Block block) {
    logger.info("Latest Block " + block.getId() + " updated for node " + this.uuid);
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
   * This function is called by this node to request the latest block from the registry node asynchronously, then it
   * sleeps the thread for a while to give time for the registry node to send the latest block. This is necessary given
   * that the simulator only support asynchronous events.
   */
  public void requestLatestBlock() {

    logger.info("node " + this.uuid + " requesting latest block");

    blockLatch = new CountDownLatch(1);
    network.send(this.getRegistryId(), new GetLatestBlockEvent(this.uuid));

    logger.info("node" + this.uuid + " waiting for latest block");

    try {
      blockLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("Node + " + this.uuid + ": latest block received");
  }


  /*
  These functions below belong solely to the registry node.
   */

  /**
   * This function is called to request a collection of transactions from the registry node.
   */
  public void requestTransactions() {

    this.transactionLatch = new CountDownLatch(1);

    network.send(this.getRegistryId(), new CollectTransactionsEvent(this.uuid, this.txMin));

    try {
      transactionLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  /**
   * Delivers requested transactions to the node.
   *
   * @param requestedTransactions List of requested transactions.
   */
  public void deliverTransactions(List<Transaction> requestedTransactions) {
    logger.info("Requested Transactions received by node " + this.uuid);
    this.requestedTransactions = requestedTransactions;
    this.transactionLatch.countDown();
  }

}
