package scenario.integrita.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;

/**
 * storage unit of the history tree. It contains methods to persist and update a database of history tree nodes.
 */
public class HistoryTreeStore implements Store {

  public HashMap<Integer, User> users;
  public HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes;

  public HistoryTreeStore() {
    this.users = new HashMap<>();
    this.historyTreeNodes = new HashMap<>();
  }

  public HistoryTreeStore(HashMap<Integer, User> users, HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes) {
    this.users = users;
    this.historyTreeNodes = historyTreeNodes;
  }

  public byte[] getVerificationKey(int user_index) {
    return users.get(user_index).verificationKey;
  }

  @Override
  public boolean insert(HistoryTreeNode historyTreeNode) {
    historyTreeNodes.put(historyTreeNode.addr, historyTreeNode);
    return true;
  }

  @Override
  public boolean insertAll(ArrayList<HistoryTreeNode> historyTreeNodes) {
    for (HistoryTreeNode node : historyTreeNodes) {
      this.insert(node);
    }
    return true;
  }

  @Override
  public boolean delete(NodeAddress nodeAddress) {
    historyTreeNodes.remove(nodeAddress);
    return true;
  }

  @Override
  public HistoryTreeNode get(NodeAddress nodeAddress) {
//    if (!historyTreeNodes.containsKey(nodeAddress)) {
//      return null;
//    }
    return historyTreeNodes.get(nodeAddress);
  }

  public int totalNodes() {
    return this.historyTreeNodes.size();
  }

  /**
   * erases all the past nodes whose `position` precede the position of the supplied `addr`
   * @param addr
   */
  public void cleanDigests(NodeAddress addr){
    for (Map.Entry<NodeAddress, HistoryTreeNode> e : historyTreeNodes.entrySet()){
      if (NodeAddress.isTreeDigest(e.getKey()) && (e.getKey().position < addr.position) && (e.getKey().position != 1)){
        historyTreeNodes.remove(e.getKey());
      }
    }
  }

}
