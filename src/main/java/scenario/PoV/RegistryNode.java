package scenario.PoV;

import Metrics.SimulatorGauge;
import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;
import org.apache.log4j.Logger;
import scenario.PoV.events.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RegistryNode implements BaseNode {

    final int transactionInsertions = 100;
    final int blockIterations = 50;
    final int numValidators = 1;
    final int txMin = 1;
    final int transactionInsertionDelay = 1000; // (ms)
    final int blockInsertionDelay = 2000; // (ms)
    final int updateWaitTime = 500; // (ms)
    /**
     * The registry node represents the underlying skip graph overlay of the LightChainNode, where it keeps the
     * list of inserted transactions and blocks, and receive requests
     * from different nodes to submit and retrieve transactions and blocks.
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
    // only for registry node
    private List<Transaction> availableTransactions;
    private List<Block> insertedBlocks;
    private Map<Integer, Integer> heightToUniquePrevCount;
    private Map<Integer, Map<UUID, Integer>> heightToUniquePrev;
    private ReadWriteLock transactionLock;
    private ReadWriteLock blockLock;
    private ReadWriteLock transactionValidationLock;
    private ReadWriteLock blockValidationLock;

    /**
     * Constructor of LightChain Node
     *
     * @param uuid    ID of the node
     * @param network used to communicate with other nodes
     */
    public RegistryNode(UUID uuid, MiddleLayer network) {
        this.uuid = uuid;
        this.network = network;
        this.transactions = new HashMap<>();
        this.blocks = new HashMap<>();
        this.transactionValidationCount = new HashMap<>();
        this.blockValidationCount = new HashMap<>();
        this.transactionValidationLock = new ReentrantReadWriteLock();
        this.blockValidationLock = new ReentrantReadWriteLock();
        this.logger = Logger.getLogger(LightChainNode.class.getName());

        // for registry nodes
        this.transactionLock = new ReentrantReadWriteLock();
        this.blockLock = new ReentrantReadWriteLock();
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


        double[] linespace = new double[this.allID.size() * this.blockIterations];

        for (int i = 0; i < linespace.length; i++) {
            linespace[i] = i;
        }

        SimulatorGauge.register("transaction_count");
        SimulatorGauge.register("block_height_per_time");
        SimulatorHistogram.register("block_height_histogram", linespace);
        SimulatorHistogram.register("unique_blocks_per_height", linespace);

        new Thread(() -> {
            monitorBlockHeight();
        }).start();

        logger.info("[Registry] The Registry node is " + this.uuid);
        this.appendBlock(new Block(UUID.randomUUID(), 0, this.uuid, UUID.randomUUID(), new ArrayList<>(), new ArrayList<>()));
        logger.info("[Registry] Genesis Block has been appended");


        network.ready();
    }

    /**
     * starts the LightChain Node by starting its iteration to insert transactions and mine blocks.
     */
    @Override
    public void onStart() {
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
        return new RegistryNode(selfID, network);
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
        for (UUID key : this.transactionValidationCount.keySet()) {

            if (this.transactionValidationCount.get(key) != this.numValidators)
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
        return true;
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

        SimulatorGauge.inc("transaction_count", this.uuid);

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

            logger.info("[Registry] number of available transactions is less than requested by node " + requester + ", required number: " + requiredNumber + ", available number: " + this.availableTransactions.size());

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

        SimulatorGauge.dec("transaction_count", this.uuid, requiredNumber);

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
            if (oldValue == null)
                oldValue = 0;
            this.heightToUniquePrevCount.put(block.getHeight(), oldValue + 1);

            SimulatorHistogram.observe("unique_blocks_per_height", this.uuid, block.getHeight());
        }

        Integer old = this.heightToUniquePrev.get(block.getHeight()).get(block.getPrev());
        if (old == null)
            old = 0;
        this.heightToUniquePrev.get(block.getHeight()).put(block.getPrev(), old + 1);


        SimulatorHistogram.observe("block_height_histogram", this.uuid, block.getHeight());

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
        long hash = latestBlock.getID().hashCode();
        int height = latestBlock.getHeight();
        Block chosenBlock = latestBlock;
        for (int i = this.insertedBlocks.size() - 1; i >= 0; --i) {

            if (this.insertedBlocks.get(i).getHeight() != height)
                break;

            long blockHash = this.insertedBlocks.get(i).getID().hashCode();

            if (blockHash < hash) {
                hash = blockHash;
                chosenBlock = this.insertedBlocks.get(i);
            }
        }
        logger.info("[Registry] " + this.insertedBlocks.size() + " blocks found");

        // this.blockLock.readLock().unlock();

        logger.info("[Registry] Sending Latest Block " + latestBlock.getID() + " to node " + requester);
        this.network.send(requester, new DeliverLatestBlockEvent(latestBlock));

        return chosenBlock;
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
