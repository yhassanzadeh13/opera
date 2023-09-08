package scenario.integrita;

import static org.junit.jupiter.api.Assertions.*;
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
    assertSame(res.get(0), StatusCode.Accept);
    assertSame(s.getStatus(), node1.addr);

    // create a history tree node whose address does not map to the server's index
    HistoryTreeNode falsenode1 = new HistoryTreeNode(new NodeAddress(2, 0), OperationType.Insert, 1);
    res = s.push(falsenode1);
    assertSame(res.get(0), StatusCode.Reject);
    assertSame(s.getStatus(), node1.addr);

    // create a history tree node whose address is not the next expected one
    HistoryTreeNode falsenode2 = new HistoryTreeNode(new NodeAddress(3, 0), OperationType.Insert, 1);
    res = s.push(falsenode2);
    assertSame(res.get(0), StatusCode.Reject);
    assertSame(s.getStatus(), node1.addr);

    // create a history tree node whose address is the next expected one
    HistoryTreeNode node2 = new HistoryTreeNode(new NodeAddress(3, 1), OperationType.Insert, 1);
    res = s.push(node2);
    assertSame(res.get(0), StatusCode.Accept);
    assertEquals(1, s.db.totalNodes()); // the temporary node should not be stored
    assertSame(s.getStatus(), node2.addr);

    // TODO unit test for signature verification

    //  add next node, it is a tree digest
    HistoryTreeNode node3 = new HistoryTreeNode(new NodeAddress(4, 2), OperationType.Insert, 1);
    res = s.push(node3);
    assertSame(res.get(0), StatusCode.Accept);
    assertEquals(2, s.db.totalNodes()); // the temporary node should not be stored
    // TODO check server signature on tree digests
    assertSame(s.getStatus(), node3.addr);

    // add next node
    // this is a tree digest, so prior tree digests should get erased
    HistoryTreeNode node4 = new HistoryTreeNode(new NodeAddress(5, 3), OperationType.Insert, 1);
    res = s.push(node4);
    assertSame(res.get(0), StatusCode.Accept);
    assertEquals(2, s.db.totalNodes()); // the temporary node should not be stored
    assertFalse(s.db.contains(node3.addr)); // the previous tree digest should be erased
    // TODO check server signature on tree digests
    assertSame(s.getStatus(), node4.addr);
  }
<<<<<<< HEAD

  @Test
  public void pullTest() {
    // set up a server
    Server s = new Server(1, 4);
    // add a user
    User user1 = new User(1, new byte[0]);
    s.db.insert(user1);

    // create a history tree node whose index maps to the server's index
    NodeAddress nodeAddress1 = new NodeAddress(1, 0);
    HistoryTreeNode node1 = new HistoryTreeNode(nodeAddress1, OperationType.Insert, 1);
    Tuple pushRes = s.push(node1);
    assertTrue(pushRes.get(0) == StatusCode.Accept);
    assertTrue(s.getStatus() == node1.addr);


    Tuple pullRes = s.pull(user1, nodeAddress1);
    assertTrue(pullRes.get(0) == node1);
    assertTrue(pullRes.get(1) != null);


  }
=======
>>>>>>> master
}
