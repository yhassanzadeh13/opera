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

  // getters and setters ---------------------------
  public byte[] getVerificationKey(int userIndex) {
    return users.get(userIndex).vk;
  }

  // utility methods -----------------------------
  public int totalNodes() {
    return this.historyTreeNodes.size();
  }

  public int totalUsers() {
    return this.users.size();
  }

  /**
   * adds all the users supplied by `users` to the `HistoryTreeStore` object.
   */
  public boolean insertAllUsers(ArrayList<User> users) {
    for (User u : users) {
      this.insert(u);
    }
    return true;
  }

  /**
   * inserts all the history nodes contained in the `historyTreeNodes` into the `HistoryTreeStore` instance.
   */
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
    Iterator<Map.Entry<NodeAddress, HistoryTreeNode>> it = historyTreeNodes.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<NodeAddress, HistoryTreeNode> entry = it.next();
      boolean isTreeDigest = NodeAddress.isTreeDigest(entry.getKey());
      if (isTreeDigest && (entry.getKey().position < addr.position) && (entry.getKey().position != 1)) {
        it.remove();
      }
    }
  }

  // ------------------- store API -----------------

  @Override
  public boolean insert(HistoryTreeNode historyTreeNode) {
    if (this.contains(historyTreeNode)) {
      return false;
    }
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
  public User get(Integer id) {
    return users.get(id);
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

  @Override
  public boolean contains(User user) {
    boolean exists = this.users.containsKey(user.id);
    return exists;
  }

  @Override
  public boolean contains(HistoryTreeNode historyTreeNode) {
    boolean exists = this.historyTreeNodes.containsKey(historyTreeNode.addr);
    return exists;
  }

  /**
   * checks if nodeAddress belongs to HistoryTreeStore object.
   */
  @Override
  public boolean contains(NodeAddress nodeAddress) {
    boolean exists = historyTreeNodes.containsKey(nodeAddress);
    return exists;
  }

}
