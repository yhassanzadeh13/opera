package scenario.integrita.historytree;


import java.util.Arrays;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode {
  public NodeAddress addr;
  public String op;
  public byte[] hash;
  public byte[] signature;
  /*
  the id of the owning user
   */
  public Integer userId;

  @Override
  public String toString() {
    return "HistoryTreeNode{" +
            "addr=" + addr +
            ", op='" + op + '\'' +
            ", hash=" + Arrays.toString(hash) +
            ", signature=" + Arrays.toString(signature) +
            ", userId=" + userId +
            '}';
  }
}
