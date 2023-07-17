package network.encoder.serializable;

import network.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncoderTest {

    private Encoder encoder;
    private Event event;

    @BeforeEach
    void setUp() {
        encoder = new Encoder();
        event = new TestEvent("Test Event");
    }

    @Test
    void encode() {
        byte[] bytes = encoder.encode(event);
        assertArrayEquals(bytes, encoder.encode(event));
    }

    @Test
    void decode() {
        byte[] bytes = encoder.encode(event);
        assertEquals(event, encoder.decode(bytes));
    }

    @Test
    void decodeShouldThrowExceptionForInvalidData() {
        byte[] invalidData = new byte[]{0, 1, 2, 3};
        assertThrows(IllegalStateException.class, () -> encoder.decode(invalidData));
    }

    static class TestEvent implements Event {
        private final String name;

        TestEvent(String name) {
            this.name = name;
        }

        // Need to override equals for the test to work.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestEvent testEvent = (TestEvent) obj;
            return name.equals(testEvent.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
