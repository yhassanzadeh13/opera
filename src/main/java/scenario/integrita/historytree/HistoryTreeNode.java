package scenario.integrita.historytree;


import java.io.Serializable;
import java.util.Arrays;

import scenario.integrita.utils.OperationType;

/**
 * implementation of a decentralized history tree as instructed in Integrita.
 */
public class HistoryTreeNode implements Serializable {
  public NodeAddress addr;
  public OperationType op;
  public String hash;
  public byte[] signature;
  /*
  the id of the owning user
   */
  public Integer userId;

  /**
   * constructor.
   *
   * @param addr   node address
   * @param op     type of operation
   * @param userId the id of the author
   */
  public HistoryTreeNode(NodeAddress addr, OperationType op, Integer userId) {
    this.addr = addr;
    this.op = op;
    this.userId = userId;
  }

  public HistoryTreeNode() {
  }

  /**
   * Sets signature for history node.
   *
   * @param signature byte representation of signature.
   */
  public void setSignature(byte[] signature) {
    this.signature = signature;
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

  /**
   * returns node's hash value concatenated with its position in the tree.
   */
  public String toLeaf() {
    return hash + addr.position;
  }
}
