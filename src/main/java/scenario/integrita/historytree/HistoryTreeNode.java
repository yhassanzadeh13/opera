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
  public String hash; // TODO: cast into its own object
  public byte[] signature; // TODO: cast into its own object
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

  /**
   * Sets hash for history node.
   *
   * @param hash string representation of hash.
   */
  public void setHash(String hash) {
    this.hash = hash;
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
