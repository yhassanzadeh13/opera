package scenario.integrita.historytree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryTreeNodeTest {

    @Test
    void testIsValid() {
        Assertions.assertFalse(NodeAddress.isValid(new NodeAddress(0, 0)));
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

    @Test
    void testmapServerIndex() {
        assertEquals(1, NodeAddress.mapServerIndex(new NodeAddress(1, 0), 4));
        assertEquals(2, NodeAddress.mapServerIndex(new NodeAddress(2, 0), 4));
        assertEquals(3, NodeAddress.mapServerIndex(new NodeAddress(2, 1), 4));
        assertEquals(4, NodeAddress.mapServerIndex(new NodeAddress(3, 0), 4));
        assertEquals(1, NodeAddress.mapServerIndex(new NodeAddress(3, 1), 4));
        assertEquals(2, NodeAddress.mapServerIndex(new NodeAddress(3, 2), 4));
        assertEquals(3, NodeAddress.mapServerIndex(new NodeAddress(4, 0), 4));
        assertEquals(4, NodeAddress.mapServerIndex(new NodeAddress(4, 1), 4));
        assertEquals(1, NodeAddress.mapServerIndex(new NodeAddress(4, 2), 4));
    }
}
