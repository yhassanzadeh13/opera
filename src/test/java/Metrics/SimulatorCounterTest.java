package Metrics;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorCounterTest{

    static final int THREAD_CNT = 50;
    static final int ITERATIONS = 50;
    static JDKRandomGenerator rand = new JDKRandomGenerator();
    CountDownLatch count;
    MetricsCollector mMetricsCollector;


    @BeforeAll
    void setup(){
        mMetricsCollector = new SimulatorCollector();
    }

    @Test
    void valueTest(){
        assertTrue(this.mMetricsCollector.Counter().register("testCounter"));
        ArrayList<UUID> allID = new ArrayList<>();
        while(allID.size() != THREAD_CNT)allID.add(UUID.randomUUID());
        count = new CountDownLatch(THREAD_CNT);

        // increment single entry
        UUID id = UUID.randomUUID();
        long tot = 0;
        for(int i = 0;i<ITERATIONS;i++) {
            int v = rand.nextInt(1000);
            tot += v;
            this.mMetricsCollector.Counter().inc("testCounter", id, v);
        }
        assertEquals(tot, this.mMetricsCollector.Counter().get("testCounter", id));

        for(UUID nodeID : allID){
            new Thread(){
                @Override
                public void run() {
                    threadtestCounter(nodeID, ITERATIONS);
                }
            }.start();
        }

        try {
            count.await();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        tot = 0;
        for(UUID nodeID : allID){
            tot += this.mMetricsCollector.Counter().get("testCounter", nodeID);
        }
        assertEquals(ITERATIONS * THREAD_CNT, tot);
    }

    void threadtestCounter(UUID nodeID, int iterations){
        while (iterations-- > 0) {
            assertTrue(this.mMetricsCollector.Counter().inc("testCounter", nodeID));
        }
        count.countDown();
    }
}