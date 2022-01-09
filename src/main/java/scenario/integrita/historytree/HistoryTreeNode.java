package scenario.integrita.historytree;


import java.util.Arrays;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode {
  public NodeAddress addr;
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

}
