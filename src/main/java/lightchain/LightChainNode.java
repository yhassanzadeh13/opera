package lightchain;

import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;
import lightchain.events.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LightChainNode implements BaseNode {

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
  private boolean isRegistry;

  private Map<UUID, Transaction> transactions;
  private Map<UUID, Block> blocks;
  private Map<UUID, Integer> transactionValidationCount;
  private Map<UUID, Integer> blockValidationCount;
  private Block latestBlock;

  final int transactionInsertions = 100;
  final int numValidators = 5;
  final int insertionDelay = 1000; // (ms)
  final int updateWaitTime = 500; // (ms)


  // only for registry node
  private List<Transaction> availableTransactions;
  private List<Block> insertedBlocks;
  private ReadWriteLock transactionLock;
  private ReadWriteLock blockLock;

  /**
   * Constructor of LightChain Node
   * @param uuid ID of the node
   * @param network used to communicate with other nodes
   */
  public LightChainNode(UUID uuid, MiddleLayer network) {
    this.uuid = uuid;
    this.network = network;
    this.transactions = new HashMap<>();
    this.blocks = new HashMap<>();
    this.transactionValidationCount = new HashMap<>();

    // for registry nodes
    this.transactionLock = new ReentrantReadWriteLock();
    this.blockLock = new ReentrantReadWriteLock();
  }

  /**
   * On the creation of a LightChain node, first the node checks if it is a registry node or not. A registry node
   * is the node of UUID placed in index 0 of allID list. This convention is pre-defined for LightChainNode. So the
   * node checks in the beginning if its UUID matches the UUID of the 0-index element of allID. If it matches then it
   * sets isRegistry variable to true, or false otherwise. If the node is the registry node, the it appends the genesis
   * block to its the list of blocks.
   * @param allID the IDs of type UUID for all the nodes in the cluster
   */
  @Override
  public void onCreate(ArrayList<UUID> allID) {
    this.allID = allID;

    // ensure that number of validators is small than number of nodes
    if(numValidators < this.allID.size() - 1) try {
      throw new Exception("Number of validators must be smaller than number of nodes");
    } catch (Exception e) {
      e.printStackTrace();
    }

    if(this.uuid.equals(this.allID.get(0)))
      this.isRegistry = true;
    else
      this.isRegistry = false;

    if(this.isRegistry) {
      this.appendBlock(new Block(UUID.randomUUID(), 0, this.uuid, UUID.randomUUID(), new ArrayList<>()));
    }
  }

  /**
   * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
   */
  @Override
  public void onStart() {

    if(!this.isRegistry)
      return ;

    this.startTransactionInsertions();
  }

  /**
   * Stops the LightChain Node
   */
  @Override
  public void onStop() {

  }

  /**
   * Performs the action of the message by passing an instance of this LightChain Node
   * @param originID the ID of the sender node
   * @param msg the content of the message
   */
  @Override
  public void onNewMessage(UUID originID, Event msg) {
    msg.actionPerformed(this);
  }

  /**
   *
   * @param selfID the ID of the new node
   * @param network communication network for the new node
   * @return a new instance of LightChainNode
   */
  @Override
  public BaseNode newInstance(UUID selfID, MiddleLayer network) {
    return new LightChainNode(selfID, network);
  }

  /**
   * This function is invoked when a validator wants to confirm their validation of a transaction. The LightChain node
   * maintains a map with a counter for every transaction it creates. This function receives the UUID of the transaction
   * that is being verified and it increases its counter. Once the counter of a transaction is equal to the number of
   * validators, this means the transaction has been validated and is ready to be inserted. So this function attempts
   * to insert the transaction into the network by sending a submit transaction event to the registry node.
   * @param transactionUUID
   */
  public void confirmTransactionValidation(UUID transactionUUID) {

    if (!transactionValidationCount.containsKey(transactionUUID)) try {
      throw new Exception("Confirming a non-existing transaction");
    } catch (Exception e) {
      e.printStackTrace();
    }

    Integer prevCount = transactionValidationCount.get(transactionUUID);
    transactionValidationCount.put(transactionUUID, prevCount + 1);

    if(prevCount + 1 == this.numValidators) {
      this.network.send(this.getRegistryID(), new SubmitTransactionEvent(this.transactions.get(transactionUUID)));
    }
  }

  /**
   * @return the UUID of the registry node
   */
  public UUID getRegistryID() {
    return this.allID.get(0);
  }

  /**
   * This function is invoked on the start of the node and it attempts to iterate and insert the required number of
   * transactions into the network. First, the node request an update of its view of the latest block Then, it gets the
   * validators of the transaction, the it asynchronously asks the validators to validate the transactions. Once the
   * transaction is validated by all validators, this will be detected at the ConfirmTransactionValidation function
   * above and that function will take care of inserting the transaction.
   *
   * TODO: This function is supposed to run either on a differen thread from the block insertion, or incorporate the
   * block insertion within it somehow
   */
  public void startTransactionInsertions() {

    for(int i = 0 ; i < this.transactionInsertions ; ++i) {
      // update the latest block
      this.requestLatestBlock();
      // get the validators
      List<UUID> validators = this.getValidators();
      // create the transaction
      Transaction tx = new Transaction(UUID.randomUUID(), this.uuid, this.latestBlock, validators);
      // initialize the counter of the transaction and store the transaction for insertion later
      this.transactionValidationCount.put(tx.getID(), 0);
      this.transactions.put(tx.getID(), tx);
      for (UUID validator : validators) {
        // send an asynchronous validation request
        network.send(validator, new ValidateTransactionEvent(tx));
      }

      // wait for some time in between insertions
      try {
        Thread.sleep(this.insertionDelay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * This function is invokes when another node requests a validation from this node. It essentially accepts
   * the validation without any conditions and immediately replies with its confirmation
   * @param transaction
   */
  public void validate(Transaction transaction) {

    network.send(transaction.getOwner(), new ConfirmTransactionEvent(transaction.getID()));
  }

  /**
   * This function randomly chooses validators of a certain transaction using the Reservoir Sampling Algorithm
   * see https://www.geeksforgeeks.org/reservoir-sampling/
   * @return a list of UUID's of randomly chosen nodes from the network
   */
  public List<UUID> getValidators() {

    // add the first numValidators nodes
    List<Integer> randomIndexes = new ArrayList<>();
    for(int i = 1 ; i <= this.numValidators ; ++i) {
      randomIndexes.add(i);
    }

    Random rand = new Random();
    for(int i = this.numValidators + 1 ; i < this.allID.size() ; ++i) {
      int j = rand.nextInt(i + 1);
      if(j < this.numValidators)
        randomIndexes.set(j, i);
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
    return this.isRegistry;
  }

  /**
   * this function is called by the registry node through an event in order to supply this node with the latest block
   * upon its an asynchronous request that was carried out earlier.
   * @param block
   */
  public void updateLatestBlock(Block block) {
    this.latestBlock = block;
  }

  /**
   * This function is called by this node to request the latest block from the registry node asynchronously, the it
   * sleeps the thread for a while to give time for the registry node to send the latest block. This is necessary given
   * that the simulator only support asynchronous events.
   */
  public void requestLatestBlock() {

    network.send(this.getRegistryID(), new GetLatestBlockEvent(this.uuid));

    try {
      Thread.sleep(this.updateWaitTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  /*
  These functions below belong solely to the registry node.
   */

  /**
   * This function is invoked from a node to insert a transaction after it has been validated.
   * It simply appends the transaction to the list of available transactions for collection. It also provides locking
   * to ensure the correctness of adding and removing transactions.
   * @param transaction transaction to be inserted into the network
   */
  public void addTransaction(Transaction transaction) {

    if(!this.isRegistry) try {
      throw new Exception("Add Transaction is called from a non-registry node");
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.transactionLock.writeLock().lock();

    this.availableTransactions.add(transaction);

    this.transactionLock.writeLock().unlock();
  }

  /**
   * This functios is invoked by a node that is inserting a block after it has been validated to the registry. It also
   * provides a lock to ensure the correctness of the write operations on the ledger.
   * @param block block to be appended to the ledger
   */
  public void appendBlock(Block block) {

    if(!this.isRegistry) try {
      throw new Exception("Add Transaction is called from a non-registry node");
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.blockLock.writeLock().lock();
    this.insertedBlocks.add(block);
    this.blockLock.writeLock().unlock();
  }

  /**
   * This function is invoked as a result of a node requesting the latest block from the registry.
   * @param requesterID of the node requesting the latest block so that its request can be delivered
   * @return the latest block on the ledger
   */
  public Block getLatestBlock(UUID requester) {

    if(!this.isRegistry) try {
      throw new Exception("Add Transaction is called from a non-registry node");
    } catch (Exception e) {
      e.printStackTrace();
    }

    Block latestBlock;

    this.blockLock.readLock().lock();
    latestBlock = this.blocks.get(this.blocks.size() - 1);
    this.blockLock.readLock().unlock();

    this.network.send(requester, new DeliverLatestBlockEvent(latestBlock));

    return latestBlock;
  }

  /**
   * This function is called from a node that is attempting to collect transaction to create a block. It takes a set
   * of transactions and returns a list of then.
   * @param requester ID of node requesting transactions
   * @param requiredNumber the required number of transactions
   * @return a list of transactions matching the number required
   */
  public List<Transaction> collectTransactions(UUID requester, Integer requiredNumber) {

    if(!this.isRegistry) try {
      throw new Exception("Add Transaction is called from a non-registry node");
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.transactionLock.readLock().lock();

    List<Transaction> requestedTransaction = new ArrayList<>();
// TODO: continue this function
//    if(this.availableTransactions.size() < requiredNumber) {
//
//    }


    this.transactionLock.readLock().unlock();

    return null;
  }
}
