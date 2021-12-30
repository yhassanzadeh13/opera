package scenario.integrita.historytree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HistoryTreeNodeTest {

  @Test
  void testIsValid() {
    assertFalse(NodeAddress.isValid(new NodeAddress(0, 0)));
    assertFalse(NodeAddress.isValid(new NodeAddress(1, -1)));
    assertFalse(NodeAddress.isValid(new NodeAddress(4, 3)));
    assertFalse(NodeAddress.isValid(new NodeAddress(3, 3)));

    assertTrue(NodeAddress.isValid(new NodeAddress(3, 2)));
    assertTrue(NodeAddress.isValid(new NodeAddress(4, 2)));
  }

  @Test
  void testIsFull() {
    assertTrue(NodeAddress.isFull(new NodeAddress(1, 0)));
    assertTrue(NodeAddress.isFull(new NodeAddress(2, 0)));
    assertTrue(NodeAddress.isFull(new NodeAddress(2, 1)));
    assertTrue(NodeAddress.isFull(new NodeAddress(3, 0)));
  }

  @Test
  void testIsTemporary() {
    assertTrue(NodeAddress.isTemporary(new NodeAddress(3, 2)));
    assertTrue(NodeAddress.isTemporary(new NodeAddress(3, 1)));
  }

  @Test
  void testIsTreeDigest() {
    assertTrue(NodeAddress.isTreeDigest(new NodeAddress(1, 0)));
    assertTrue(NodeAddress.isTreeDigest(new NodeAddress(2, 1)));
    assertTrue(NodeAddress.isTreeDigest(new NodeAddress(3, 2)));
    assertTrue(NodeAddress.isTreeDigest(new NodeAddress(4, 2)));

  }
}
