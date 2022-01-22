package scenario.integrita;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.lang.Tuple;
import org.junit.jupiter.api.Test;
import scenario.integrita.historytree.HistoryTreeNode;
import scenario.integrita.historytree.NodeAddress;
import scenario.integrita.user.User;
import scenario.integrita.utils.OperationType;
import scenario.integrita.utils.StatusCode;

public class ServerTest {

  @Test
  public void pushTestServerIndex() {
    // set up a server
    Server s = new Server(1, 4);
    // add a user
    s.db.insert(new User(1, new byte[0]));

    // create a history tree node whose index maps to the server's index
    HistoryTreeNode node1 = new HistoryTreeNode(new NodeAddress(1, 0), OperationType.Insert, 1);
    Tuple res = s.push(node1);
    assertTrue(res.get(0) == StatusCode.Accept);

    // create a history tree node whose address does not map to the server's index
    HistoryTreeNode falsenode1 = new HistoryTreeNode(new NodeAddress(2, 0), OperationType.Insert, 1);
    res = s.push(falsenode1);
    assertTrue(res.get(0) == StatusCode.Reject);

    // create a history tree node whose address is not the next expected one
    HistoryTreeNode falsenode2 = new HistoryTreeNode(new NodeAddress(3, 0), OperationType.Insert, 1);
    res = s.push(falsenode2);
    assertTrue(res.get(0) == StatusCode.Reject);

    // create a history tree node whose address is the next expected one
    HistoryTreeNode node2 = new HistoryTreeNode(new NodeAddress(3, 1), OperationType.Insert, 1);
    res = s.push(node2);
    assertTrue(res.get(0) == StatusCode.Accept);
    assertTrue(s.db.totalNodes() == 1); // the temporary node should not be stored

    // TODO unit test for signature verification

    //  add next node
    HistoryTreeNode node3 = new HistoryTreeNode(new NodeAddress(4, 2), OperationType.Insert, 1);
    res = s.push(node3);
    assertTrue(res.get(0) == StatusCode.Accept);
    assertTrue(s.db.totalNodes() == 2); // the temporary node should not be stored

    // add next node
    // this is a tree digest, so prior tree digests should get erased
    HistoryTreeNode node4 = new HistoryTreeNode(new NodeAddress(5, 3), OperationType.Insert, 1);
    res = s.push(node4);
    assertTrue(res.get(0) == StatusCode.Accept);
    assertTrue(s.db.totalNodes() == 2); // the temporary node should not be stored
    assertFalse(s.db.contains(node3.addr)); // the previous tree digest should be erased
  }

  @Test
  public void pushTestLabelDistance() {
    // set server's status

    // create a history tree node whose index has more than N difference from the server's status
  }

  @Test
  public void pushTestTemporaryNodes() {
    // create a temporary history tree node
    // push it to the server
    // the db should not change
  }

  @Test
  public void pushTestInsertTreeDigest() {
    // populate a server with proper nodes of the history tree
    // create a tree digest
    // push it to the server
    // check internal state of the server
    // the size of db
    // the state variable
  }
}
