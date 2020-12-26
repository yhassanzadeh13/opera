package SkipGraph.skipnode;


import java.util.List;

public interface SkipNodeInterface {

    /**
     * Add the SkipNode to the SkipGraph through an introducer.
     * @param introducerAddress the address of the introducer.
     * @param introducerPort the port of the introducer.
     */
    void insert(String introducerAddress, int introducerPort);

    /**
     * Returns whether the node is available to be used as a router. If the node is still being inserted,
     * then, this will return false.
     * @return whether the node is available for routing or not.
     */
    boolean isAvailable();

    /**
     * Finds the `ladder`, i.e. the node that should be used to propagate a newly joined node to the upper layer.
     * @return the ladder's node information.
     */
    SkipNodeIdentity findLadder(int level, int direction, String target);

    /**
     * Adds the given neighbor to the appropriate lookup table entries of this node. Should only be used during concurrent
     * insertion (i.e., ConcurrentBackupTable is being used.)
     * @param newNeighbor the identity of the new neighbor.
     * @param minLevel the minimum level in which the new neighbor should be connected.
     */
    void announceNeighbor(SkipNodeIdentity newNeighbor, int minLevel);

    /**
     * Remove the node from the SkipGraph. Joins the neighbors on each level together
     * @return True is successful, false otherwise
     */
    boolean delete();

    /**
     * Search for the given numID
     * @param numID The numID to search for
     * @return The SkipNodeIdentity of the node with the given numID. If it does not exist, returns the SkipNodeIdentity of the SkipNode with NumID closest to the given
     * numID from the direction the search is initiated.
     * For example: Initiating a search for a SkipNode with NumID 50 from a SnipNode with NumID 10 will return the SkipNodeIdentity of the SnipNode with NumID 50 is it exists. If
     * no such SnipNode exists, the SkipNodeIdentity of the SnipNode whose NumID is closest to 50 among the nodes whose NumID is less than 50 is returned.
     */
    SkipNodeIdentity searchByNumID(int numID);

    /**
     * Search for the given nameID
     * @param nameID The nameID to search for
     * @return The SkipNodeIdentity of the SkipNode with the given nameID. If it does not exist, returns the SkipNodeIdentity of the SkipNode which shares the longest
     * prefix among the nodes in the SkipGraph. Also contains the piggybacked information.
     */
    SearchResult searchByNameID(String nameID);

    /**
     * Used by the `searchByNameID` method. Implements a recursive name ID search algorithm.
     * @param target the target name ID.
     * @param level the current level.
     * @return the identity of the node with the given name ID, or the node with the closest name ID.
     */
    SearchResult searchByNameIDRecursive(String target, int level);

    /**
     * Updates the SkipNode on the left on the given level to the given SkipNodeIdentity
     * @param snId The new SkipNodeIdentity to be placed in the given level
     * @param level The level to place the given SkipNodeIdentity
     * @return The SkipNodeIdentity that was replaced (Could be an EMPTY_NODE)
     */
    SkipNodeIdentity updateLeftNode(SkipNodeIdentity snId, int level);

    /**
     * Updates the SkipNode on the right on the given level to the given SkipNodeIdentity
     * @param snId The new SkipNodeIdentity to be placed in the given level
     * @param level The level to place the given SkipNodeIdentity
     * @return The SkipNodeIdentity that was replaced (Could be an EMPTY_NODE)
     */
    SkipNodeIdentity updateRightNode(SkipNodeIdentity snId, int level);

    /**
     * Returns the up-to-date identity of this node.
     * @return the up-to-date identity of this node.
     */
    SkipNodeIdentity getIdentity();

    /**
     * Returns the right neighbor of the node at the given level.
     * @param level the level of the right neighbor.
     * @return the right neighbor at the given level.
     */
    SkipNodeIdentity getRightNode(int level);

    /**
     * Returns the left neighbor of the node at the given level.
     * @param level the level of the left neighbor.
     * @return the left neighbor at the given level.
     */
    SkipNodeIdentity getLeftNode(int level);

    /**
     *
     * @param owner
     * @return
     */
    boolean unlock(SkipNodeIdentity owner);

    /**
     *
     * @param requester
     * @return
     */
    boolean tryAcquire(SkipNodeIdentity requester, int version);

    /**
     *
     * @return
     */
    boolean isLocked();

    /**
     *
     * @param address
     * @param port
     * @return
     */
    boolean isLockedBy(String address, int port);

    /*
    Test
     */
    SkipNodeIdentity increment(SkipNodeIdentity snId, int level);
    boolean inject(List<SkipNodeIdentity> injections);
}
