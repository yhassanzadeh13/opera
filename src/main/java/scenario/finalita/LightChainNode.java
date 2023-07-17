package scenario.finalita;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Event;
import node.BaseNode;
import node.Identifier;
import node.IdentifierGenerator;
import scenario.finalita.events.CollectTransactionsEvent;
import scenario.finalita.events.ConfirmBlockEvent;
import scenario.finalita.events.ConfirmTransactionEvent;
import scenario.finalita.events.DeliverLatestBlockEvent;
import scenario.finalita.events.DeliverTransactionsEvent;
import scenario.finalita.events.GetLatestBlockEvent;
import scenario.finalita.events.SubmitBlockEvent;
import scenario.finalita.events.SubmitTransactionEvent;
import scenario.finalita.events.ValidateBlockEvent;
import scenario.finalita.events.ValidateTransactionEvent;
import scenario.finalita.metrics.LightChainMetrics;


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
  private static final Random random = new Random();
  final int transactionInsertions = 100;
  final int blockIterations = 50;
  final int numValidators = 1;
  final int txMin = 1;
  final int transactionInsertionDelay = 1000; // (ms)
  final int blockInsertionDelay = 2000; // (ms)
  final int updateWaitTime = 500; // (ms)
  private Logger logger;
  private List<Identifier> allId;
  private Identifier identifier;
  private network.Network network;
  private boolean isRegistry;
  private Map<Identifier, Transaction> transactions;
  private Map<Identifier, Block> blocks;
  private Map<Identifier, Integer> transactionValidationCount;
  private Map<Identifier, Integer> blockValidationCount;
  private Block latestBlock;
  private List<Transaction> requestedTransactions;
  private CountDownLatch blockLatch;
  private CountDownLatch transactionLatch;
  private Integer maximumHeight;
  private Integer totalTransactionCount;
  private LightChainMetrics lightChainMetrics;
  // only for registry node
  private List<Transaction> availableTransactions;
  private List<Block> insertedBlocks;
  private Map<Integer, Integer> heightToUniquePrevCount;
  private Map<Integer, Map<Identifier, Integer>> heightToUniquePrev;
  private ReadWriteLock transactionValidationLock;
  private ReadWriteLock blockValidationLock;

  /**
   * Constructor of LightChain Node.
   *
   * @param nodeId  identifier of the node
   * @param network used to communicate with other nodes
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "network is externally mutable")
  public LightChainNode(Identifier nodeId, network.Network network) {
    this.identifier = nodeId;
    this.network = network;
    this.transactions = new HashMap<>();
    this.blocks = new HashMap<>();
    this.transactionValidationCount = new HashMap<>();
    this.blockValidationCount = new HashMap<>();
    this.transactionValidationLock = new ReentrantReadWriteLock();
    this.blockValidationLock = new ReentrantReadWriteLock();
    this.lightChainMetrics = new LightChainMetrics();
    this.logger = OperaLogger.getLoggerForNodeComponent(LightChainNode.class.getCanonicalName(),
            nodeId,
            "lightchain-node");

    // for registry nodes
    this.availableTransactions = new ArrayList<>();
    this.insertedBlocks = new ArrayList<>();
    this.heightToUniquePrev = new HashMap<>();
    this.heightToUniquePrevCount = new HashMap<>();
    this.maximumHeight = 0;
    this.totalTransactionCount = 0;
  }

  // TODO: do we need this?
  public LightChainNode() {
  }

  /**
   * On the creation of a LightChain node, first the node checks if it is a registry node or not.
   * A registry node is the node of Identifier placed in index 0 of allID list.
   * This convention is pre-defined for LightChainNode.
   * So the node checks if its Identifier matches the UUID of the 0-index element of allID.
   * If it matches then it sets isRegistry variable to true, or false otherwise.
   * If the node is the registry node, then it appends the genesis block to its list of blocks.
   *
   * @param allId the IDs of type Identifier for all the nodes in the cluster
   */
  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "allId is externally mutable")
  public void onCreate(ArrayList<Identifier> allId) {
    logger.info("node created");
    this.allId = allId;

    // ensure that number of validators is small than number of nodes
    if (numValidators > this.allId.size() - 1) {
      // TODO: throw exception.
      this.logger.fatal("number of validators {} is greater than number of nodes {}",
              numValidators,
              this.allId.size() - 1);
    }

    // TODO: this is a hack, separate registry into a different class.
    this.isRegistry = this.identifier.equals(this.allId.get(0));

    if (this.isRegistry) {
      double[] linespace = new double[this.allId.size() * this.blockIterations];

      for (int i = 0; i < linespace.length; i++) {
        linespace[i] = i;
      }


      this.logger.info("the registry node is created");
      Block genesisBlock = new Block(IdentifierGenerator.newIdentifier(),
              0,
              this.identifier,
              IdentifierGenerator.newIdentifier(),
              new ArrayList<>(),
              new ArrayList<>());
      this.appendBlock(genesisBlock);
      this.logger.info("genesis block is created, block id {}", genesisBlock.getId());
    }

    network.ready();
  }

  /**
   * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {
    if (this.isRegistry) {
      return;
    }
    logger.info("lightchain node starts");

    // TODO: keep track of threads and stop them on stop.
    new Thread(this::startTransactionInsertions).start();
    new Thread(this::startBlockInsertion).start();
  }

  /**
   * Stops the LightChain Node.
   */
  @Override
  public void onStop() {
    logger.info("lightchain node stops");
  }

  /**
   * Performs the action of the message by passing an instance of this LightChain Node.
   *
   * @param originId the ID of the sender node
   * @param event    the content of the message
   */
  @Override
  public void onNewMessage(Identifier originId, Event event) {
    if (event instanceof CollectTransactionsEvent) {
      if (!this.isRegistry()) {
        throw new IllegalStateException("CollectTransactionsEvent is only for registry node");
      }
      CollectTransactionsEvent collectTransactionsEvent = (CollectTransactionsEvent) event;
      this.collectTransactions(collectTransactionsEvent.getRequester(), collectTransactionsEvent.getRequiredNumber());
      return;
    } else if (event instanceof ConfirmBlockEvent) {
      ConfirmBlockEvent confirmBlockEvent = (ConfirmBlockEvent) event;
      this.confirmBlockValidation(confirmBlockEvent.getBlockId());
      return;
    } else if (event instanceof DeliverLatestBlockEvent) {
      DeliverLatestBlockEvent deliverLatestBlockEvent = (DeliverLatestBlockEvent) event;
      this.updateLatestBlock(deliverLatestBlockEvent.getLatestBlock());
    } else if (event instanceof DeliverTransactionsEvent) {
      DeliverTransactionsEvent deliverTransactionsEvent = (DeliverTransactionsEvent) event;
      this.deliverTransactions(deliverTransactionsEvent.getTransactions());
      return;
    } else if (event instanceof GetLatestBlockEvent) {
      if (!this.isRegistry) {
        throw new IllegalStateException("Submit Transaction Event is submitted to a node other than registry");
      }
      GetLatestBlockEvent getLatestBlockEvent = (GetLatestBlockEvent) event;
      this.getLatestBlock(getLatestBlockEvent.getRequester());
      return;
    } else if (event instanceof SubmitBlockEvent) {
      if (!this.isRegistry) {
        throw new IllegalStateException("Submit Transaction Event is submitted to a node other than registry");
      }
      SubmitBlockEvent submitBlockEvent = (SubmitBlockEvent) event;
      this.appendBlock(submitBlockEvent.getBlock());
      return;
    } else if (event instanceof SubmitTransactionEvent) {
      if (this.isRegistry) {
        throw new IllegalStateException("Submit Transaction Event is submitted to registry");
      }
      this.addTransaction(((SubmitTransactionEvent) event).getTransaction());
    } else if (event instanceof ValidateBlockEvent) {
      this.validateBlock(((ValidateBlockEvent) event).getBlock());
      return;
    } else if (event instanceof ValidateTransactionEvent) {
      this.validateTransaction(((ValidateTransactionEvent) event).getTransaction());
      return;
    }
    throw new IllegalStateException("Unknown message type: " + event.getClass().getName());
  }

  /**
   * creates a new instance of LightChainNode.
   *
   * @param selfId    the ID of the new node
   * @param nameSpace string tag to virtually group the nodes (with identical tags)
   * @param network   communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network) {
    return new LightChainNode(selfId, network);
  }

  /**
   * Invoked by a node that is inserting a block after it has been validated to the registry.
   * It also provides a lock to ensure the correctness of the write operations on the ledger.
   *
   * @param block block to be appended to the ledger
   */
  public void appendBlock(Block block) {
    if (!this.isRegistry) {
      this.logger.fatal("only registry node can append block, block id {}", block.getId());
    }

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
    }

    Integer old = this.heightToUniquePrev.get(block.getHeight()).get(block.getPrev());
    if (old == null) {
      old = 0;
    }
    this.heightToUniquePrev.get(block.getHeight()).put(block.getPrev(), old + 1);

    this.logger.info(
            "registry node added new block to the ledger, block id {}, height {}, maximum height {}, blocks in {}",
            block.getId(),
            block.getHeight(),
            this.maximumHeight,
            this.insertedBlocks.size());
    this.lightChainMetrics.onNewFinalizedBlock(block.getHeight(), block.getId(), block.getOwner());
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
    for (int i = 0; i < this.transactionInsertions; ++i) {

      this.logger.info("inserting transaction number {}", i + 1);

      // update the latest block
      Identifier latestBlockId = this.requestLatestBlock();

      this.logger.info("latest block id {} updated", latestBlockId);

      // get the validators
      List<Identifier> validators = this.getValidators();
      // create the transaction
      Transaction tx = new Transaction(IdentifierGenerator.newIdentifier(),
              this.identifier,
              this.latestBlock,
              validators);
      // initialize the counter of the transaction and store the transaction for insertion later
      this.transactionValidationCount.put(tx.getId(), 0);
      this.transactions.put(tx.getId(), tx);


      for (Identifier validator : validators) {
        // send an asynchronous validation request
        network.send(validator, new ValidateTransactionEvent(tx));
        this.logger.info("requesting validator {} for transaction {}", validator, tx.getId());
      }

      // wait for some time in between insertions
      // TODO: this is a hack, we should use a proper synchronization mechanism.
      try {
        Thread.sleep(this.transactionInsertionDelay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    int count = 0;
    for (Map.Entry<Identifier, Integer> e : this.transactionValidationCount.entrySet()) {

      if (e.getValue() < this.numValidators) {
        count += 1;
      }
    }

    this.logger.info("number of non-validated transactions pending {}", count);
  }

  /**
   * This function handles the collection of transactions and casting them into blocks and then inserting these blocks.
   * to the registry
   */
  public void startBlockInsertion() throws IllegalStateException {
    for (int i = 0; i < this.blockIterations; ++i) {

      try {
        Thread.sleep(this.blockInsertionDelay);
      } catch (InterruptedException e) {
        throw new IllegalStateException("block insertion thread interrupted", e);
      }

      this.logger.info("block collection attempt is {}", i + 1);

      this.requestTransactions();
      this.requestLatestBlock();

      List<Transaction> collectedTransaction = this.requestedTransactions;

      List<Identifier> transactionIds = new ArrayList<>();
      for (Transaction tx : collectedTransaction) {
        transactionIds.add(tx.getId());
      }

      if (collectedTransaction.isEmpty()) {
        this.logger.info("transaction collection attempt is {}", i + 1);
        continue;
      }

      List<Identifier> validators = getValidators();
      Block block = new Block(IdentifierGenerator.newIdentifier(),
              this.latestBlock.getHeight() + 1,
              this.identifier,
              this.latestBlock.getId(),
              validators,
              transactionIds);

      this.blockValidationCount.put(block.getId(), 0);
      this.blocks.put(block.getId(), block);


      for (Identifier validator : validators) {
        // send an asynchronous validation request
        network.send(validator, new ValidateBlockEvent(block));
        this.logger.info("node is requesting validator {} for block {}", validator, block.getId());
      }
    }
  }

  /**
   * This function is invoked when a validator wants to confirm their validation of a transaction. The LightChain node
   * maintains a map with a counter for every transaction it creates. This function receives the Identifier of the
   * transaction
   * that is being verified, and it increases its counter. Once the counter of a transaction is equal to the number of
   * validators, this means the transaction has been validated and is ready to be inserted. So this function attempts
   * to insert the transaction into the network by sending submit transaction event to the registry node.
   *
   * @param txId unique Id of the trasaction.
   */
  public void confirmTransactionValidation(Identifier txId) throws IllegalStateException {
    if (!transactionValidationCount.containsKey(txId)) {
      throw new IllegalStateException("received confirmation for a non-existing transaction: " + txId);
    }

    // TODO: encapsulate this in a function.
    this.transactionValidationLock.writeLock().lock();
    Integer validationCount = transactionValidationCount.get(txId);
    validationCount = validationCount + 1;
    this.logger.info("node received confirmation for transaction {} from validator, validation count is {}",
            txId,
            validationCount + 1);
    transactionValidationCount.put(txId, validationCount);
    this.transactionValidationLock.writeLock().unlock();

    if (validationCount + 1 >= this.numValidators) {
      // TODO: we should remove transaction from the validation count map and add its id to a processed transaction map.
      this.logger.info("node is inserting transaction {} on the overlay network", txId);
      this.network.send(this.getRegistryId(), new SubmitTransactionEvent(this.transactions.get(txId)));
    }
  }

  /**
   * getter of registry node's ID.
   *
   * @return the Identifier of the registry node.
   */
  public Identifier getRegistryId() {
    return this.allId.get(0);
  }

  /**
   * Confirms validation of the block with its Identifier.
   *
   * @param blockId Identifier of the block.
   */
  public void confirmBlockValidation(Identifier blockId) throws IllegalStateException {
    if (!blockValidationCount.containsKey(blockId)) {
      throw new IllegalStateException("received confirmation for a non-existing block: " + blockId);
    }

    // TODO: encapsulate this in a function.
    this.blockValidationLock.writeLock().lock();
    Integer validationCount = blockValidationCount.get(blockId);
    validationCount = validationCount + 1;
    this.logger.info("node received confirmation for block {} from validator, validation count is {}",
            blockId,
            validationCount + 1);
    blockValidationCount.put(blockId, validationCount);
    this.blockValidationLock.writeLock().unlock();

    if (validationCount + 1 >= this.numValidators) {
      // TODO: we should remove block from the validation count map and add its id to a processed block map.
      this.logger.info("node is inserting block {} on the overlay network", this.identifier, blockId);
      this.network.send(this.getRegistryId(), new SubmitBlockEvent(this.blocks.get(blockId)));
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
   * @return a list of Identifiers of randomly chosen nodes from the network
   */
  // TODO: this function must be a random persistent oracle, not a random function.
  public List<Identifier> getValidators() {

    logger.info("fetching validators for node");

    // add the first numValidators nodes
    List<Integer> randomIndexes = new ArrayList<>();
    for (int i = 1; i <= this.numValidators; ++i) {
      randomIndexes.add(i);
    }

    for (int i = this.numValidators + 1; i < this.allId.size(); ++i) {
      int j = random.nextInt(i + 1);
      if (j < this.numValidators) {
        randomIndexes.set(j, i);
      }
    }

    List<Identifier> validators = new ArrayList<>();
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
    return this.isRegistry;
  }

  /**
   * this function is called by the registry node through an event in order to supply this node with the latest block
   * upon its asynchronous request that was carried out earlier.
   *
   * @param block block to update the node
   */
  public void updateLatestBlock(Block block) {
    this.logger.info("latest block updated to {}", block.getId());
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
  public Identifier requestLatestBlock() {

    this.logger.info("node requesting latest block");

    blockLatch = new CountDownLatch(1);
    network.send(this.getRegistryId(), new GetLatestBlockEvent(this.identifier));

    this.logger.info("node is waiting for latest block");

    try {
      blockLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.logger.info("latest block received: {}", this.latestBlock.getId());

    return this.latestBlock.getId();
  }


  /*
  These functions below belong solely to the registry node.
   */

  /**
   * This function is called to request a collection of transactions from the registry node.
   */
  public void requestTransactions() {

    this.transactionLatch = new CountDownLatch(1);

    network.send(this.getRegistryId(), new CollectTransactionsEvent(this.identifier, this.txMin));

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
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "requestedTransactions is externally mutable")
  public void deliverTransactions(List<Transaction> requestedTransactions) {
    this.logger.info("requested Transactions received"); // todo: adding transaction ids to the log.
    this.requestedTransactions = requestedTransactions;
    this.transactionLatch.countDown();
  }

  /**
   * This function is invoked from a node to insert a transaction after it has been validated.
   * It simply appends the transaction to the list of available transactions for collection. It also provides locking
   * to ensure the correctness of adding and removing transactions.
   *
   * @param transaction transaction to be inserted into the network
   */
  public void addTransaction(Transaction transaction) throws IllegalStateException {
    if (!this.isRegistry) {
      throw new IllegalStateException("add Transaction is called from a non-registry node");
    }


    // TODO: do we need lock?
    //  this.transactionLock.writeLock().lock();

    this.availableTransactions.add(transaction);
    this.totalTransactionCount += 1;
    logger.info("new transaction inserted on the registry node, available transactions: {} total transactions: {}",
            this.availableTransactions.size(),
            this.totalTransactionCount);
    this.lightChainMetrics.onNewTransactions(1);

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
  public List<Transaction> collectTransactions(Identifier requester, Integer requiredNumber) throws
          IllegalStateException {
    if (!this.isRegistry) {
      throw new IllegalStateException("collect transactions is called from a non-registry node");
    }

    // TODO: do we need lock?

    //   this.transactionLock.writeLock().lock();
    List<Transaction> requestedTransactions = new ArrayList<>();
    if (this.availableTransactions.size() < requiredNumber) {
      logger.info("number of available transactions ({}) in registry is less than requested ({}) by node ({})",
              this.availableTransactions.size(),
              requiredNumber,
              requester);
      //    this.transactionLock.writeLock().unlock();
      network.send(requester, new DeliverTransactionsEvent(requestedTransactions));
      return requestedTransactions;
    }

    // TODO: revisit this part
    for (Transaction availableTransaction : this.availableTransactions) {
      requestedTransactions.add(availableTransaction);
    }

    List<Transaction> temporary = new ArrayList<>();
    for (int i = requiredNumber; i < this.availableTransactions.size(); ++i) {
      temporary.add(this.availableTransactions.get(i));
    }
    this.availableTransactions = temporary;

    //  this.transactionLock.writeLock().unlock();

    network.send(requester, new DeliverTransactionsEvent(requestedTransactions));

    return null;
  }

  /**
   * This function is invoked as a result of a node requesting the latest block from the registry.
   *
   * @param requester of the node requesting the latest block so that its request can be delivered
   * @return the latest block on the ledger
   */
  public Block getLatestBlock(Identifier requester) throws IllegalStateException {
    if (!this.isRegistry) {
      throw new IllegalStateException("get latest block is called from a non-registry node");
    }
    logger.debug("registry received a request for latest block from node {}", requester);

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
    logger.debug("registry found {} block as the latest block for request of node {}", chosenBlock.getId(), requester);

    // this.blockLock.readLock().unlock();

    this.network.send(requester, new DeliverLatestBlockEvent(latestBlock));
    this.logger.info("registry delivered latest block {} to node {}", latestBlock.getId(), requester);

    return chosenBlock;
  }
}
