package Underlay;

import Metrics.MetricsCollector;
import Metrics.SimulatorCollector;
import Underlay.Local.LocalUnderlay;
import Underlay.TCP.TCPUnderlay;
import Underlay.UDP.UDPUnderlay;
import Underlay.javaRMI.JavaRMIUnderlay;
import Utils.NoopOrchestrator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test the communication and termination of every network underlay
 */

public class UnderlayTest {
    static final int THREAD_CNT = 50;
    static final int START_PORT = 2000;
    static final int SLEEP_DURATION = 1000;
    static final double EPS = 0.01;
    static JDKRandomGenerator rand = new JDKRandomGenerator();
    CountDownLatch count;
    ArrayList<FixtureNode> instances = new ArrayList<>();
    ArrayList<UUID> allID = new ArrayList<>();
    private MetricsCollector mMetricsCollector;
    private final HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    private final HashMap<AbstractMap.SimpleEntry<String, Integer>, Underlay> allUnderlays = new HashMap<>();
    private final HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

    @BeforeEach
    void initialize() {
        instances.clear();
        allID.clear();
        allUnderlays.clear();
        isReady.clear();
        mMetricsCollector = new SimulatorCollector();

        // generate IDs
        for (int i = 0; i < THREAD_CNT; i++) {
            allID.add(UUID.randomUUID());
        }

        // generate full addresses
        try {
            String address = Inet4Address.getLocalHost().getHostAddress();
            for (int i = 0; i < THREAD_CNT; i++) {
                allFullAddresses.put(allID.get(i), new AbstractMap.SimpleEntry<>(address, START_PORT + i));
                isReady.put(new AbstractMap.SimpleEntry<>(address, START_PORT + i), true);
            }

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Test
    void A_testTCP() {
        // generate middle layers
        for (int i = 0; i < THREAD_CNT; i++) {
            UUID id = allID.get(i);
            String address = allFullAddresses.get(id).getKey();
            int port = allFullAddresses.get(id).getValue();

            MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), mMetricsCollector);
            FixtureNode node = new FixtureNode(id, allID, middleLayer);
            middleLayer.setOverlay(node);
            TCPUnderlay underlay = new TCPUnderlay();
            underlay.initialize(port, middleLayer);
            middleLayer.setUnderlay(underlay);
            instances.add(node);
            allUnderlays.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
        }
        assure();
        terminate();
    }

    void assure() {
        // start all instances
        for (FixtureNode node : instances) {
            new Thread(node::onStart).start();
        }
        try {
            Thread.sleep(SLEEP_DURATION);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // check that all nodes received threadCount - 1 messages
        for (FixtureNode node : instances) {
            assertEquals(THREAD_CNT - 1, node.receivedMessages.get());
        }
    }

    void terminate() {
        for (Map.Entry<AbstractMap.SimpleEntry<String, Integer>, Underlay> entry : allUnderlays.entrySet())
            entry.getValue().terminate(entry.getKey().getKey(), entry.getKey().getValue());
    }

    @Test
    void B_testUDP() {
        // generate middle layers
        for (int i = 0; i < THREAD_CNT; i++) {
            UUID id = allID.get(i);
            String address = allFullAddresses.get(id).getKey();
            int port = allFullAddresses.get(id).getValue();

            MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), mMetricsCollector);
            FixtureNode node = new FixtureNode(id, allID, middleLayer);
            middleLayer.setOverlay(node);
            UDPUnderlay underlay = new UDPUnderlay();
            underlay.initialize(port, middleLayer);
            middleLayer.setUnderlay(underlay);
            instances.add(node);
            allUnderlays.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
        }

        assure();
        terminate();
    }

    @Test
    void C_testRMI() {
        // generate middle layers
        for (int i = 0; i < THREAD_CNT; i++) {
            UUID id = allID.get(i);
            String address = allFullAddresses.get(id).getKey();
            int port = allFullAddresses.get(id).getValue();

            MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), mMetricsCollector);
            FixtureNode node = new FixtureNode(id, allID, middleLayer);
            middleLayer.setOverlay(node);
            JavaRMIUnderlay underlay = new JavaRMIUnderlay();
            underlay.initialize(port, middleLayer);
            middleLayer.setUnderlay(underlay);
            instances.add(node);
            allUnderlays.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
        }
        assure();
        terminate();
    }

    @Test
    void testLocal() {
        HashMap<AbstractMap.SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
        // generate middle layers
        for (int i = 0; i < THREAD_CNT; i++) {
            UUID id = allID.get(i);
            String address = allFullAddresses.get(id).getKey();
            int port = allFullAddresses.get(id).getValue();
            MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), mMetricsCollector);
            FixtureNode node = new FixtureNode(id, allID, middleLayer);
            middleLayer.setOverlay(node);
            LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
            underlay.initialize(port, middleLayer);
            middleLayer.setUnderlay(underlay);
            instances.add(node);
            allLocalUnderlay.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
        }
        assure();
    }
}
