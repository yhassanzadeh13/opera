package scenario.integrita;

import org.junit.jupiter.api.Test;

public class ServerTest {

  @Test
  public void pushTestServerIndex(){
    // create a history tree node whose index maps to the server's index
    Server s = new Server();
    s.index = 1;
    s.totalServers = 4;


    // create a history tree node whose index does not map to the server's index
  }

  @Test
  public void pushTestLabelDistance(){
    // set server's status

    // create a history tree node whose index has more than N difference from the server's status
  }

  @Test
  public void pushTestTemporaryNodes(){
    // create a temporary history tree node
    // push it to the server
    // the db should not change
  }

  @Test
  public void pushTestInsertTreeDigest(){
    // populate a server with proper nodes of the history tree
    // create a tree digest
    // push it to the server
    // check internal state of the server
    // the size of db
    // the state variable
  }
}
