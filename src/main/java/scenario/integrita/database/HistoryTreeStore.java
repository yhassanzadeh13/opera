package scenario.integrita.database;

import java.util.ArrayList;
import java.util.HashMap;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;

/**
 * storage unit of the history tree. It contains methods to persist and update a database of history tree nodes.
 */
public class HistoryTreeStore implements Store {

  public ArrayList<User> users;
  public HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes;

  public HistoryTreeStore() {
    this.users = new ArrayList<>();
    this.historyTreeNodes = new HashMap<>();
  }

  public HistoryTreeStore(ArrayList<User> users, HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes) {
    this.users = users;
    this.historyTreeNodes = historyTreeNodes;
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

  public Integer totalNodes() {
    return this.historyTreeNodes.size();
  }
}
