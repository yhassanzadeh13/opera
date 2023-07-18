package scenario.integrita.database;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;
import scenario.integrita.utils.OperationType;

public class HisoryTreeStoreTest {

  /**
   * populates a HistoryTreeStore with all the nodes of a history tree at its vth version
   *
   * @param v
   * @return
   */
  public HistoryTreeStore initHistoryTreeStore(int v) {
    HistoryTreeStore historyTreeStore = new HistoryTreeStore();
    for (int p = 1; p <= v; p++) {
      for (int l = 0; l <= NodeAddress.maxLevel(p); l++) {
        HistoryTreeNode historyTreeNode = new HistoryTreeNode(new NodeAddress(p, l), OperationType.Insert, 1);
        historyTreeStore.insert(historyTreeNode);
      }
    }
    return historyTreeStore;

  }

  @Test
  public void TestHistoryNodes() {
    HistoryTreeStore historyTreeStore = new HistoryTreeStore();

    // add 3 users
    historyTreeStore.insert(new User(1));
    historyTreeStore.insert(new User(2));
    historyTreeStore.insert(new User(3));

    // create three nodes, two of which have the same address
    HistoryTreeNode historyTreeNode1 = new HistoryTreeNode(new NodeAddress(0, 1), OperationType.Delete, 1);
    // historyTreeNode2  points to the same address as historyTreeNode1
    HistoryTreeNode historyTreeNode2 = new HistoryTreeNode(new NodeAddress(0, 1), OperationType.Insert, 2);

    HistoryTreeNode historyTreeNode3 = new HistoryTreeNode(new NodeAddress(2, 1), OperationType.Insert, 2);

    historyTreeStore.insertAllNodes(new ArrayList<HistoryTreeNode>(Arrays.asList(historyTreeNode1, historyTreeNode2, historyTreeNode3)));

    // test get method
    // insertion of historyTreeNode1 should get through but not historyTreeNode2
    assertSame(historyTreeStore.get(historyTreeNode1.addr), historyTreeNode1);

    // duplicates are not allowed
    assertTrue((historyTreeStore.totalNodes() == 2));

    // test contains method
    assertTrue(historyTreeStore.contains(historyTreeNode1));
    assertTrue(historyTreeStore.contains(historyTreeNode3));

    historyTreeStore.delete(historyTreeNode1.addr);
    // check the correct deletion
    assertTrue((historyTreeStore.totalNodes() == 1));
    // only historyTreeNode3 is left
    assertTrue(historyTreeStore.contains(historyTreeNode3));

    // test get method
    assertSame(historyTreeStore.get(historyTreeNode3.addr), historyTreeNode3);
  }

  @Test
  public void TestUsers() {
    HistoryTreeStore historyTreeStore = new HistoryTreeStore();

    // add 3 users
    User u1 = new User(1);
    User u2 = new User(2);
    User u3 = new User(3);
    historyTreeStore.insertAllUsers(new ArrayList<User>(Arrays.asList(u1, u1, u2, u3)));


    // check whether the inserted users are correctly included
    // insertion of historyTreeNode1 should get through but not historyTreeNode2
    assertSame(historyTreeStore.get(u1.id), u1);
    assertSame(historyTreeStore.get(u2.id), u2);
    assertSame(historyTreeStore.get(u3.id), u3);

    // duplicates are not allowed
    assertTrue((historyTreeStore.totalUsers() == 3));

    // test contains method
    assertTrue(historyTreeStore.contains(u1));
    assertTrue(historyTreeStore.contains(u2));

    // test delete
    historyTreeStore.delete(u3);
    // check the correct deletion
    assertFalse(historyTreeStore.contains(u3));
  }

  @Test
  public void testCleanTreeDigest() {
    HistoryTreeStore historyTreeStore = initHistoryTreeStore(5);
    // check the inclusion of all the tree digests up to version 5 of the tree
    assertTrue(historyTreeStore.contains(new NodeAddress(1, 0)));
    assertTrue(historyTreeStore.contains(new NodeAddress(2, NodeAddress.maxLevel(2))));
    assertTrue(historyTreeStore.contains(new NodeAddress(3, NodeAddress.maxLevel(3))));
    assertTrue(historyTreeStore.contains(new NodeAddress(4, NodeAddress.maxLevel(4))));
    assertTrue(historyTreeStore.contains(new NodeAddress(5, NodeAddress.maxLevel(5))));
    historyTreeStore.cleanDigests(new NodeAddress(4, 0));
    // tree digests of 2nd, and 3rd operations should be deleted
    assertTrue(historyTreeStore.contains(new NodeAddress(1, 0)));
    assertFalse(historyTreeStore.contains(new NodeAddress(2, NodeAddress.maxLevel(2))));
    assertFalse(historyTreeStore.contains(new NodeAddress(3, NodeAddress.maxLevel(3))));
    assertTrue(historyTreeStore.contains(new NodeAddress(4, NodeAddress.maxLevel(4))));
    assertTrue(historyTreeStore.contains(new NodeAddress(5, NodeAddress.maxLevel(5))));
  }
}
