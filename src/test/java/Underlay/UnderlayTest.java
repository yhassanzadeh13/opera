package Underlay;

import Metrics.NoopCollector;
import Underlay.Local.LocalUnderlay;
import Utils.NoopOrchestrator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test the communication and termination of every network underlay
 */

public class UnderlayTest {
    static final int THREAD_CNT = 50;
    static final int START_PORT = 2000;
    static final int PORT_RANGE = 10000;
    static final int SLEEP_DURATION = 1000;
    static JDKRandomGenerator rand = new JDKRandomGenerator();

    private final ConcurrentHashMap<Integer, Integer> usedPorts = new ConcurrentHashMap();
    private static final HashMap<AbstractMap.SimpleEntry<String, Integer>, Underlay> allUnderlays = new HashMap<>();


    ArrayList<FixtureNode> initialize(String underlayName) {
        ArrayList<FixtureNode> instances = new ArrayList<>();
        ArrayList<UUID> allID = new ArrayList<>();
        HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
        HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

        // generate IDs
        for (int i = 0; i < THREAD_CNT; i++) {
            allID.add(UUID.randomUUID());
        }

        // generate full addresses
        try {
            String address = Inet4Address.getLocalHost().getHostAddress();
            for (int i = 0; i < THREAD_CNT; i++) {
                int port;

                do{
                    port = rand.nextInt(PORT_RANGE) + START_PORT;
                }while(usedPorts.containsKey(port));
                usedPorts.putIfAbsent(port, 1);


                allFullAddresses.put(allID.get(i), new AbstractMap.SimpleEntry<>(address, port));
                isReady.put(new AbstractMap.SimpleEntry<>(address, port), true);
            }

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < THREAD_CNT; i++) {
                UUID id = allID.get(i);
                String address = allFullAddresses.get(id).getKey();
                int port = allFullAddresses.get(id).getValue();

                MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), new NoopCollector());
                FixtureNode node = new FixtureNode(id, allID, middleLayer);
                middleLayer.setOverlay(node);

                Underlay underlay = UnderlayFactory.NewUnderlay(underlayName, port, middleLayer);
                middleLayer.setUnderlay(underlay);
                instances.add(node);
                allUnderlays.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
            }
        } catch (Exception e) {
            for (Integer port:
            this.usedPorts.values()) {
                System.out.println("PORT = "+ port);
            }
        }


        return instances;
    }

    @Test
    void A_testTCP() {
        // generate middle layers
        ArrayList<FixtureNode> TCPNodes = initialize("tcp");
        assure(TCPNodes);

    }

    void assure(ArrayList<FixtureNode> instances) {
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

    @AfterAll
    static void terminate() {
        for (Map.Entry<AbstractMap.SimpleEntry<String, Integer>, Underlay> entry : allUnderlays.entrySet())
            entry.getValue().terminate(entry.getKey().getKey(), entry.getKey().getValue());
    }

    @Test
    void B_testUDP() {
        // generate middle layers
        ArrayList<FixtureNode> UDPNodes = initialize("udp");
        assure(UDPNodes);
    }

    @Test
    void C_testRMI() {
        // generate middle layers
        ArrayList<FixtureNode> javaRMINodes = initialize("javaRMI");
        assure(javaRMINodes);
    }

    @Test
    void testLocal() {
        HashMap<AbstractMap.SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay = new HashMap<>();
        // generate middle layers
        ArrayList<FixtureNode> instances = new ArrayList<>();
        ArrayList<UUID> allID = new ArrayList<>();
        HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
        HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();

        // generate IDs
        for (int i = 0; i < THREAD_CNT; i++) {
            allID.add(UUID.randomUUID());
        }

        // generate full addresses
        try {
            String address = Inet4Address.getLocalHost().getHostAddress();
            for (int i = 0; i < THREAD_CNT; i++) {
                int startingPort;
                do{
                    startingPort = rand.nextInt(PORT_RANGE) + START_PORT;
                }while(usedPorts.containsKey(startingPort));
                usedPorts.put(startingPort, 1);

                allFullAddresses.put(allID.get(i), new AbstractMap.SimpleEntry<>(address, startingPort));
                isReady.put(new AbstractMap.SimpleEntry<>(address, startingPort), true);
            }

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < THREAD_CNT; i++) {
            UUID id = allID.get(i);
            String address = allFullAddresses.get(id).getKey();
            int port = allFullAddresses.get(id).getValue();

            MiddleLayer middleLayer = new MiddleLayer(id, allFullAddresses, isReady, new NoopOrchestrator(), new NoopCollector());
            FixtureNode node = new FixtureNode(id, allID, middleLayer);
            middleLayer.setOverlay(node);

            LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
            underlay.initialize(port, middleLayer);

            middleLayer.setUnderlay(underlay);
            instances.add(node);
            allLocalUnderlay.put(new AbstractMap.SimpleEntry<>(address, port), underlay);
        }

        assure(instances);
    }
}
