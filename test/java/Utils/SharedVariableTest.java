package Utils;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class SharedVariableTest {

    // increase the sleeping duration when increasing the thread count and iterations
    static final int THREAD_CNT = 50;
    static final int ITERATIONS = 30;

    static  JDKRandomGenerator rand = new JDKRandomGenerator();
    static HashMap<UUID, Long> values = new HashMap<>();
    CountDownLatch count;
    @Test
    void read_write() {
        // test unregistered variable
        assertFalse(SharedVariable.write(UUID.randomUUID(), "Test", 5));

        ArrayList<UUID> allID = new ArrayList<>();
        while(allID.size() != THREAD_CNT)allID.add(UUID.randomUUID());
        count = new CountDownLatch(THREAD_CNT);


        // register new variable
        assertTrue(SharedVariable.register("Test", allID));
        // attempt to write from node 1
        assertFalse(SharedVariable.write(allID.get(0), "Test", 5));
        // acquire lock then write
        assertTrue(SharedVariable.requestLock(allID.get(0), "Test"));
        assertTrue(SharedVariable.write(allID.get(0), "Test", 5));
        // attempt to acquire lock from node 2
        assertFalse(SharedVariable.requestLock(allID.get(1), "Test"));
        // release lock then acquire lock from node 2
        SharedVariable.releaseLock(allID.get(0), "Test");
        assertTrue(SharedVariable.requestLock(allID.get(1), "Test"));
        // read from node 2
        assertEquals(new AbstractMap.SimpleEntry<>(allID.get(0), 5), SharedVariable.read(allID.get(1), "Test"));
        // read from node 3
        assertEquals(new AbstractMap.SimpleEntry<>(allID.get(0), 5), SharedVariable.read(allID.get(2), "Test"));
        // read again from node 3 should throws an exception
        assertNull(SharedVariable.read(allID.get(2), "Test"));
        // unregistered variable
        try{
            SharedVariable.read(allID.get(2), "Test1");
            fail();
        }catch (Exception e){
            assertTrue(true);
        }

        assertTrue(SharedVariable.register("Count", allID));
        for(UUID id : allID)
            values.put(id, 1L);
        for(UUID id : allID) {
            new Thread(){
                @Override
                public void run() {
                    threadTest(id, ITERATIONS);
                }
            }.start();
        }
        try{
            count.await();
        }catch (Exception e){

        }
        long sample = -1;
        for(UUID nodeID : allID){
            long x = 1;
            while(!SharedVariable.isEmpty(nodeID, "Count")){
                x = (long) SharedVariable.read(nodeID, "Count").getValue();
                values.put(nodeID, values.get(nodeID) | x);
            }
            if(sample == -1){
                sample = values.get(nodeID);
            }
            else assertEquals(sample, values.get(nodeID));
        }
    }

    private void threadTest(UUID nodeID, int iterations){
        while(iterations-- > 0) {
            assertFalse(SharedVariable.write(nodeID, "Count", 1));
            if (rand.nextBoolean()) continue;

            long x = 1;
            while (!SharedVariable.isEmpty(nodeID, "Count")) {
                x = (long) SharedVariable.read(nodeID, "Count").getValue();
                values.put(nodeID, values.get(nodeID) | x);
            }
            x *= 2;
            if (rand.nextBoolean() && SharedVariable.requestLock(nodeID, "Count")) {
                assertTrue(SharedVariable.write(nodeID, "Count", x));
                values.put(nodeID, values.get(nodeID) | x);
                SharedVariable.releaseLock(nodeID, "Count");
            } else {
                assertFalse(SharedVariable.write(nodeID, "Count", x));
            }
        }
        count.countDown();
    }
}