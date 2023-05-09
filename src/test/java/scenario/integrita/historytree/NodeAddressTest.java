package scenario.integrita.historytree;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


public class NodeAddressTest {
  @Test
  public void toLabelTest() {
    assertTrue(NodeAddress.toLabel(new NodeAddress(1, 0)) == 1);

    assertTrue(NodeAddress.toLabel(new NodeAddress(2, 0)) == 2);
    assertTrue(NodeAddress.toLabel(new NodeAddress(2, 1)) == 3);

    assertTrue(NodeAddress.toLabel(new NodeAddress(3, 0)) == 4);
    assertTrue(NodeAddress.toLabel(new NodeAddress(3, 1)) == 5);
    assertTrue(NodeAddress.toLabel(new NodeAddress(3, 2)) == 6);

    assertTrue(NodeAddress.toLabel(new NodeAddress(4, 0)) == 7);
    assertTrue(NodeAddress.toLabel(new NodeAddress(4, 1)) == 8);
    assertTrue(NodeAddress.toLabel(new NodeAddress(4, 2)) == 9);
  }
}
