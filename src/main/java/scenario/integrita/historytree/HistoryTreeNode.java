package scenario.integrita.historytree;


import java.util.Arrays;

import scenario.integrita.utils.OperationType;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode {
  public NodeAddress addr;
  public OperationType op;
  public String hash;
  public byte[] signature;
  /*
  the id of the owning user
   */
  public Integer userId;

  public HistoryTreeNode(NodeAddress addr, OperationType op, Integer userId) {
    this.addr = addr;
    this.op = op;
    this.userId = userId;
  }

  public HistoryTreeNode() {
  }

  @Override
  public String toString() {
    return "HistoryTreeNode{"
            + "addr=" + addr
            + ", op='" + op + '\''
            + ", hash=" + hash
            + ", signature=" + Arrays.toString(signature)
            + ", userId=" + userId
            + '}';
  }
}
