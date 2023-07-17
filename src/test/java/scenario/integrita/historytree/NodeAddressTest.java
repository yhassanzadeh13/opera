package scenario.integrita.historytree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class NodeAddressTest {
    @Test
    public void toLabelTest() {
        assertEquals(1, NodeAddress.toLabel(new NodeAddress(1, 0)));

        assertEquals(2, NodeAddress.toLabel(new NodeAddress(2, 0)));
        assertEquals(3, NodeAddress.toLabel(new NodeAddress(2, 1)));

        assertEquals(4, NodeAddress.toLabel(new NodeAddress(3, 0)));
        assertEquals(5, NodeAddress.toLabel(new NodeAddress(3, 1)));
        assertEquals(6, NodeAddress.toLabel(new NodeAddress(3, 2)));

        assertEquals(7, NodeAddress.toLabel(new NodeAddress(4, 0)));
        assertEquals(8, NodeAddress.toLabel(new NodeAddress(4, 1)));
        assertEquals(9, NodeAddress.toLabel(new NodeAddress(4, 2)));
    }
}
