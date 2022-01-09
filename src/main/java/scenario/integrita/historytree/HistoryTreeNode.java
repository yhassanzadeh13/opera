package scenario.integrita.historytree;


import java.util.Arrays;

import scenario.integrita.utils.OperationType;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode {
  public NodeAddress addr;
  OperationType op;
  byte[] hash;
  byte[] signature;

  public HistoryTreeNode(NodeAddress addr, OperationType op, byte[] signature) {
    this.addr = addr;
    this.op = op;
    this.signature = signature;
  }

  public HistoryTreeNode() {
  }

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
