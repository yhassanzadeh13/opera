package scenario.integrita.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;
import scenario.integrita.utils.OperationType;

public class HisoryTreeStoreTest {

  @Test
  public void TestInsertDeleteGet() {
    HistoryTreeStore historyTreeStore = new HistoryTreeStore();

    // add 3 users
    historyTreeStore.users.put( 1, new User(1));
    historyTreeStore.users.put( 2, new User(2));
    historyTreeStore.users.put( 2, new User(2));

    // create three nodes, two of which have the same address
    HistoryTreeNode historyTreeNode1 = new HistoryTreeNode(new NodeAddress(0, 1), OperationType.Insert, 1);
    // historyTreeNode2  points to the same address as historyTreeNode1
    HistoryTreeNode historyTreeNode2 = new HistoryTreeNode(new NodeAddress(0, 1), OperationType.Insert, 2);

    HistoryTreeNode historyTreeNode3 = new HistoryTreeNode(new NodeAddress(2, 1), OperationType.Insert, 2);

    historyTreeStore.insertAll(new ArrayList<HistoryTreeNode>(Arrays.asList(historyTreeNode1, historyTreeNode2, historyTreeNode3)));

    // duplicates are not allowed
    assertTrue((historyTreeStore.totalNodes() == 2));

    historyTreeStore.delete(historyTreeNode1.addr);
    // check the correct deletion
    assertTrue((historyTreeStore.totalNodes() == 1));
    // only historyTreeNode3 is left
    assertTrue((historyTreeStore.get(historyTreeNode3.addr) == historyTreeNode3));
  }
}
