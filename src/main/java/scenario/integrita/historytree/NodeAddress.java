package scenario.integrita.historytree;

/**
 * holds the address of a node of the history tree.
 * position represents the operation number.
 * level indicates at which height of the history tree the node is located.
 * level ranges from 0 to log2(position)+1.
 */
public class NodeAddress {
  int position;
  int level;
}
