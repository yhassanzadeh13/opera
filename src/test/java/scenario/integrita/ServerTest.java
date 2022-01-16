package scenario.integrita;

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
    Server s = new Server();
    s.index = 1;
    s.totalServers = 4;
    s.db.insert(new User());
    // create a history tree node whose index maps to the server's index
    HistoryTreeNode historyTreeNode = new HistoryTreeNode(new NodeAddress(1, 0), OperationType.Insert, 1);
    Tuple res = s.push(historyTreeNode);
    assertTrue(res.get(0) == StatusCode.Accept);



    // create a history tree node whose index does not map to the server's index
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
