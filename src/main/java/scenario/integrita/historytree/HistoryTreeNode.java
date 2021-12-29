package scenario.integrita.historytree;


import java.util.Arrays;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode {
  NodeAddress addr;
  String op;
  byte[] hash;
  byte[] signature;

  @Override
  public String toString() {
    return "HistoryTreeNode{"
            + "addr=" + addr
            + ", op='" + op + '\''
            + ", hash=" + Arrays.toString(hash)
            + ", signature=" + Arrays.toString(signature)
            + '}';
  }

  /**
   * checks if the supplied address `addr` belongs to a full node.
   * A full node is a node whose left and right sub-trees are full.
   */
  public boolean isFull(NodeAddress addr) {
    int modulus = (int) Math.pow(2, addr.level);
    int remainder = Math.floorMod(addr.position, modulus);
    return (remainder == 0);
  }


}
