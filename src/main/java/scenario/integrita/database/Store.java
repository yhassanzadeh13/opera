package scenario.integrita.database;

import java.util.ArrayList;

import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;

/**
 * the storage api
 */
public interface Store {
  public boolean insert(HistoryTreeNode historyTreeNode);
  public boolean insertAll(ArrayList<HistoryTreeNode> historyTreeNodes);
  public boolean delete(NodeAddress nodeAddress);
  public HistoryTreeNode get(NodeAddress nodeAddress);
}
