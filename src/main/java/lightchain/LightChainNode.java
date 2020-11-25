package lightchain;

import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;
import lightchain.events.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LightChainNode implements BaseNode {

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

  @Override
  public void onStart() {

    if(!this.isRegistry)
      return ;

    this.startTransactionInsertions();
  }

  @Override
  public void onStop() {

  }

  @Override
  public void onNewMessage(UUID originID, Event msg) {

  }

  @Override
  public BaseNode newInstance(UUID selfID, MiddleLayer network) {
    return new LightChainNode(selfID, network);
  }


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

  public UUID getRegistryID() {
    return this.allID.get(0);
  }


  public void startTransactionInsertions() {

    for(int i = 0 ; i < this.transactionInsertions ; ++i) {

      this.requestLatestBlock();

      List<UUID> validators = this.getValidators();

      Transaction tx = new Transaction(UUID.randomUUID(), this.uuid, this.latestBlock, validators);

      this.transactionValidationCount.put(tx.getID(), 0);
      this.transactions.put(tx.getID(), tx);
      for (UUID validator : validators) {

        network.send(validator, new ValidateTransactionEvent(tx));
      }

      try {
        Thread.sleep(this.insertionDelay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void validate(Transaction transaction) {

    network.send(transaction.getOwner(), new ConfirmTransactionEvent(transaction.getID()));
  }

  public List<UUID> getValidators() {

    // use reservoir sampling to pick validators randomly
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

  public boolean isRegistry() {
    return this.isRegistry;
  }

  public void updateLatestBlock(Block block) {
    this.latestBlock = block;
  }

  public void requestLatestBlock() {

    network.send(this.getRegistryID(), new GetLatestBlockEvent(this.uuid));

    try {
      Thread.sleep(this.updateWaitTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  // functions for registry node

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
