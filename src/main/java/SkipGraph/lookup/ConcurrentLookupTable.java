package SkipGraph.lookup;

import SkipGraph.skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConcurrentLookupTable is a lookup table that supports concurrent calls
 */
public class ConcurrentLookupTable implements LookupTable {
    private final int numLevels;
    private ReadWriteLock lock;
    /**
     * All the neighbors are placed in an arraylist, with EMPTY_NODE for empty nodes.
     * The formula to get the index of a neighbor is 2*level for a node on the left side
     * and 2*level+1 for a node on the right side. This is reflected in the getIndex
     * method.
     */
    private ArrayList<SkipNodeIdentity> nodes;

    private enum direction {
        LEFT,
        RIGHT
    }

    public ConcurrentLookupTable(int numLevels) {
        this.numLevels = numLevels;
        lock = new ReentrantReadWriteLock(true);
        nodes = new ArrayList<>(2*numLevels);
        for(int i = 0; i < 2 * numLevels; i++){
            nodes.add(i, LookupTable.EMPTY_NODE);
        }
    }

    @Override
    public SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.LEFT, level);
        if(idx >= nodes.size()) {
            lock.writeLock().unlock();
            return LookupTable.EMPTY_NODE;
        }
        SkipNodeIdentity prev = nodes.set(idx, node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity updateRight(SkipNodeIdentity node, int level) {
        lock.writeLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        if(idx >= nodes.size()) {
            lock.writeLock().unlock();
            return LookupTable.EMPTY_NODE;
        }
        SkipNodeIdentity prev = nodes.set(idx, node);
        lock.writeLock().unlock();
        return prev;
    }

    @Override
    public SkipNodeIdentity getRight(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.RIGHT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public SkipNodeIdentity getLeft(int level) {
        lock.readLock().lock();
        int idx = getIndex(direction.LEFT, level);
        SkipNodeIdentity node = (idx < nodes.size()) ? nodes.get(idx) : LookupTable.EMPTY_NODE;
        lock.readLock().unlock();
        return node;
    }

    @Override
    public List<SkipNodeIdentity> getRights(int level) {
        List<SkipNodeIdentity> ls = new ArrayList<>(1);
        SkipNodeIdentity id = getRight(level);
        if(!id.equals(LookupTable.EMPTY_NODE)) ls.add(id);
        return ls;
    }

    @Override
    public List<SkipNodeIdentity> getLefts(int level) {
        List<SkipNodeIdentity> ls = new ArrayList<>(1);
        SkipNodeIdentity id = getLeft(level);
        if(!id.equals(LookupTable.EMPTY_NODE)) ls.add(id);
        return ls;
    }

    @Override
    public boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level) {
        lock.readLock().lock();
        boolean exists = getLeft(level).equals(neighbor);
        lock.readLock().unlock();
        return exists;
    }

    @Override
    public boolean isRightNeighbor(SkipNodeIdentity neighbor, int level) {
        lock.readLock().lock();
        boolean exists = getRight(level).equals(neighbor);
        lock.readLock().unlock();
        return exists;
    }

    @Override
    public SkipNodeIdentity removeLeft(int level) {
        return updateLeft(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public SkipNodeIdentity removeRight(int level) {
        return updateRight(LookupTable.EMPTY_NODE, level);
    }

    @Override
    public int getNumLevels() {
        return this.numLevels;
    }

    /**
     * Returns the new neighbors (unsorted) of a newly inserted node. It is assumed that the newly inserted node
     * will be a neighbor to the owner of this lookup table.
     * @param owner the identity of the owner of the lookup table.
     * @param newNameID the name ID of the newly inserted node.
     * @param newNumID the num ID of the newly inserted node.
     * @param level the level of the new neighbor.
     * @return the list of neighbors (both right and left) of the newly inserted node.
     */
    @Override
    public TentativeTable acquireNeighbors(SkipNodeIdentity owner, int newNumID, String newNameID, int level) {
        lock.readLock().lock();
        List<List<SkipNodeIdentity>> newTable = new ArrayList<>();
        newTable.add(new ArrayList<>());
        newTable.get(0).add(owner);
        if(newNumID < owner.getNumID() && !getLeft(level).equals(LookupTable.EMPTY_NODE))
            newTable.get(0).add(getLeft(level));
        else if(!getRight(level).equals(LookupTable.EMPTY_NODE))
            newTable.get(0).add(getRight(level));
        lock.readLock().unlock();
        return new TentativeTable(false, level, newTable);
    }

    /**
     * Given an incomplete tentative table, inserts the given level neighbors to their correct positions.
     * @param owner the owner of the lookup table.
     * @param tentativeTable the tentative table containing list of potential neighbors.
     */
    @Override
    public void initializeTable(SkipNodeIdentity owner, TentativeTable tentativeTable) {
        SkipNodeIdentity left = tentativeTable.neighbors.get(0).stream()
                .filter(x -> x.getNumID() <= owner.getNumID())
                .findFirst()
                .orElse(LookupTable.EMPTY_NODE);
        SkipNodeIdentity right = tentativeTable.neighbors.get(0).stream()
                .filter(x -> x.getNumID() > owner.getNumID())
                .findFirst()
                .orElse(LookupTable.EMPTY_NODE);
        updateLeft(left, tentativeTable.specificLevel);
        updateRight(right, tentativeTable.specificLevel);
    }

    private int getIndex(direction dir, int level) {
        if(level < 0) return Integer.MAX_VALUE;
        if(dir== direction.LEFT) {
            return level * 2;
        }else{
            return level * 2 + 1;
        }
    }
}
