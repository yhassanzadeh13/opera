package SkipGraph.lookup;

import SkipGraph.skipnode.SkipNodeIdentity;

import java.util.List;

public interface LookupTable {

    SkipNodeIdentity EMPTY_NODE = new SkipNodeIdentity("EMPTY", -1, "EMPTY", -1);
    SkipNodeIdentity INVALID_NODE = new SkipNodeIdentity("INVALID", -1, "INVALID", -1);

    /**
     * Updates the left neighbor on the given level to be the node
     * @param node Node to be put on the lookup table
     * @param level The level on which to insert the node
     * @return Replaced node
     */
    SkipNodeIdentity updateLeft(SkipNodeIdentity node, int level);

    /**
     * Updates the right neighbor on the given level to be the node
     * @param node Node to be put on the lookup table
     * @param level The level on which to insert the node
     * @return Replaced node
     */
    SkipNodeIdentity updateRight(SkipNodeIdentity node, int level);

    /**
     * Returns the best right neighbor on the given level
     * @param level The level to get the node from
     * @return The right neighbor on the given level
     */
    SkipNodeIdentity getRight(int level);

    /**
     * Returns the best left neighbor on the given level
     * @param level The level to get the node from
     * @return The left neighbor on the given level
     */
    SkipNodeIdentity getLeft(int level);

    /**
     * Returns a list of all the right neighbors on the given level.
     * @param level The level to get the node from
     * @return the list of the right neighbors.
     */
    List<SkipNodeIdentity> getRights(int level);

    /**
     * Returns a list of all the left neighbors on the given level.
     * @param level The level to get the node from
     * @return the list of the left neighbors.
     */
    List<SkipNodeIdentity> getLefts(int level);

    /**
     * Remove the left neighbor on the given level
     * @param level The level from which to remove the left neighbor
     * @return Removed node
     */
    SkipNodeIdentity removeLeft(int level);

    /**
     * Remove the right neighbor on the given level
     * @param level The level from which to remove the right neighbor
     * @return Removed node
     */
    SkipNodeIdentity removeRight(int level);

    /**
     * Returns whether the given left neighbor exists in this lookup table at the given level.
     * @param neighbor the neighbor to check existence of.
     * @param level the level of the neighbor.
     * @return true iff the neighbor is a left neighbor at the given level.
     */
    boolean isLeftNeighbor(SkipNodeIdentity neighbor, int level);

    /**
     * Returns whether the given right neighbor exists in this lookup table at the given level.
     * @param neighbor the neighbor to check existence of.
     * @param level the level of the neighbor.
     * @return true iff the neighbor is a right neighbor at the given level.
     */
    boolean isRightNeighbor(SkipNodeIdentity neighbor, int level);

    /**
     * Get the number of levels in the lookup table
     * @return The number of levels in the lookup table
     */
    int getNumLevels();

    /**
     * Returns the new neighbors (unsorted) of a newly inserted node. It is assumed that the newly inserted node
     * will be a neighbor to the owner of this lookup table.
     * @param owner the identity of the owner of the lookup table.
     * @param newNameID the name ID of the newly inserted node.
     * @param newNumID the num ID of the newly inserted node.
     * @param level the level of the new neighbor.
     * @return the list of neighbors (both right and left) of the newly inserted node.
     */
    TentativeTable acquireNeighbors(SkipNodeIdentity owner, int newNumID, String newNameID, int level);

    /**
     * Given a list of potential neighbors, inserts them at the appropriate positions. This should only be called
     * during the insertion of the owner of the lookup table.
     * @param owner the owner of the lookup table.
     * @param tentativeTable the tentative table containing list of potential neighbors.
     */
    void initializeTable(SkipNodeIdentity owner, TentativeTable tentativeTable);

}
