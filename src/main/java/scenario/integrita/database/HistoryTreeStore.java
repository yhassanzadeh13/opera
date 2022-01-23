package scenario.integrita.database;

import java.util.*;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;

/**
 * storage unit of the history tree. It contains methods to persist and update a database of history tree nodes.
 */
public class HistoryTreeStore implements Store {

  private final HashMap<Integer, User> users;
  private final HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes;

  // constructor ----------------------------------------------
  public HistoryTreeStore() {
    this.users = new HashMap<>();
    this.historyTreeNodes = new HashMap<>();
  }

  public HistoryTreeStore(HashMap<Integer, User> users, HashMap<NodeAddress, HistoryTreeNode> historyTreeNodes) {
    this.users = users;
    this.historyTreeNodes = historyTreeNodes;
  }


  // getters and setters ---------------------------
  public byte[] getVerificationKey(int userIndex) {
    return users.get(userIndex).vk;
  }

  // utility methods -----------------------------
  public int totalNodes() {
    return this.historyTreeNodes.size();
  }

  public boolean insertAllUsers(ArrayList<User> users) {
    for (User u : users) {
      this.insert(u);
    }
    return true;
  }

  public boolean insertAllNodes(ArrayList<HistoryTreeNode> historyTreeNodes) {
    for (HistoryTreeNode node : historyTreeNodes) {
      this.insert(node);
    }
    return true;
  }

  /**
   * erases all the past nodes whose `position` precede the position of the supplied `addr` exclusively.
   */
  public void cleanDigests(NodeAddress addr) {
    Set<NodeAddress> keySet = historyTreeNodes.keySet();
    Iterator<Map.Entry<NodeAddress, HistoryTreeNode>> it = historyTreeNodes.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<NodeAddress, HistoryTreeNode> entry = it.next();
      boolean isTreeDigest = NodeAddress.isTreeDigest(entry.getKey());
      if (isTreeDigest && (entry.getKey().position < addr.position) && (entry.getKey().position != 1)) {
        it.remove();
      }
    }
  }

  /**
   * checks if nodeAddress belongs to HistoryTreeStore object.
   */
  public boolean contains(NodeAddress nodeAddress) {
    boolean exists = historyTreeNodes.containsKey(nodeAddress);
    return exists;
  }

  // ------------------- store API -----------------

  @Override
  public boolean insert(HistoryTreeNode historyTreeNode) {
    historyTreeNodes.put(historyTreeNode.addr, historyTreeNode);
    return true;
  }

  @Override
  public boolean insert(User user) {
    // TODO check duplicates
    users.put(user.id, user);
    return true;
  }

  @Override
  public HistoryTreeNode get(NodeAddress nodeAddress) {
    return historyTreeNodes.get(nodeAddress);
  }

  @Override
  public User get(User user) {
    return users.get(user.id);
  }

  @Override
  public boolean delete(NodeAddress nodeAddress) {
    historyTreeNodes.remove(nodeAddress);
    return true;
  }

  @Override
  public boolean delete(User user) {
    users.remove(user.id);
    return true;
  }

//  @Override
//  public boolean contains(User user);
//  boolean contains(HistoryTreeNode historyTreeNode);

}
