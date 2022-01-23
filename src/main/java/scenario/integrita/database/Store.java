package scenario.integrita.database;

import java.util.ArrayList;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;

/**
 * storage api.
 */
public interface Store {

  boolean insert(HistoryTreeNode historyTreeNode);
  boolean insert(User user);

  HistoryTreeNode get(NodeAddress nodeAddress);
  User get(User user);

  boolean delete(NodeAddress nodeAddress);
  boolean delete(User user);

  boolean contains(User user);
  boolean contains(HistoryTreeNode historyTreeNode);

}
