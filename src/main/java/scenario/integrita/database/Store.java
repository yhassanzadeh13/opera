package scenario.integrita.database;

import java.util.ArrayList;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;

/**
 * storage api.
 */
public interface Store {

  boolean insert(HistoryTreeNode historyTreeNode);

  boolean insertAll(ArrayList<HistoryTreeNode> historyTreeNodes);

  boolean delete(NodeAddress nodeAddress);

  HistoryTreeNode get(NodeAddress nodeAddress);

}
