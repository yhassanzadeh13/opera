package Scenario;

import underlay.UnderlayType;
import metrics.MetricsCollector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import examples.bullyalgorithm.MyNode;
import simulator.Simulator;

import java.util.Random;

import java.util.UUID;

public class BullyAlgorithmTest {
    private MetricsCollector metrics;
    /**
     * Tests the bully algorithm for only one node.
     */
    @Test
    void OneNode_Test(){
        MyNode fixtureNode = new MyNode();
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, 1, UnderlayType.MOCK_NETWORK);
        fixtureNode.setSimulation(simulation);
        simulation.constantSimulation(1000);
        //assertEquals(fixtureNode.getCoordinatorID(),fixtureNode.getCoordinatorID());
        assertEquals(fixtureNode.getMaxId(),fixtureNode.getCoordinatorId());
    }

    /**
     * Tests the bully algorithm for two nodes with two cases:
     * Case1- Nodes have same UUID
     * Case2- Nodes have different UUID
     */
    @Test
    void TwoNode_Test(){
        //For same UUID's
        MyNode fixtureNode1 = new MyNode();
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode1, 1, UnderlayType.MOCK_NETWORK);
        UUID uuid = simulation.getAllId().get(0);
        simulation.newInstance(uuid,simulation.getMiddleLayer(uuid),metrics);
        fixtureNode1.setSimulation(simulation);
        simulation.constantSimulation(1000);
        assertEquals(fixtureNode1.getMaxId(),fixtureNode1.getCoordinatorId());

        //For different UUID's
        MyNode fixtureNode2 = new MyNode();
        Simulator<MyNode> simulation1 = new Simulator<MyNode>(fixtureNode2, 2, UnderlayType.MOCK_NETWORK);
        UUID uuid3 = simulation1.getAllId().get(0);
        simulation.newInstance(UUID.randomUUID(),simulation.getMiddleLayer(uuid3),metrics);
        fixtureNode2.setSimulation(simulation);
        simulation1.constantSimulation(1000);
        assertEquals(fixtureNode2.getMaxId(),fixtureNode2.getCoordinatorId());
    }

    /**
     * Tests the bully algorithm for random number of nodes in 3 cases:
     * Case1- All nodes have same UUID
     * Case2- Some nodes have same UUID
     * Case3- All nodes have different UUID
     */
    @Test
    void MoreNode_Test(){
        Random rand = new Random();
        int min = 3;
        int max = 10;
        int N = rand.nextInt(max - min) + min;

        //For all same UUID's
        MyNode fixtureNode1 = new MyNode();
        Simulator<MyNode> simulation1 = new Simulator<MyNode>(fixtureNode1, N, UnderlayType.MOCK_NETWORK);
        UUID uuid1 = simulation1.getAllId().get(0);
        for(int i = 0; i < N; i++) {
            simulation1.newInstance(uuid1, simulation1.getMiddleLayer(uuid1),metrics);
        }
        fixtureNode1.setSimulation(simulation1);
        simulation1.constantSimulation(1000);
        assertEquals(fixtureNode1.getMaxId(),fixtureNode1.getCoordinatorId());

        //For some of UUID's are same
        MyNode fixtureNode2 = new MyNode();
        Simulator<MyNode> simulation2 = new Simulator<MyNode>(fixtureNode2, N, UnderlayType.MOCK_NETWORK);
        UUID uuid2 = simulation2.getAllId().get(0);
        simulation2.newInstance(uuid2, simulation1.getMiddleLayer(uuid2),metrics);
        for(int i = 1; i < N; i++) {
            simulation2.newInstance(UUID.randomUUID(), simulation2.getMiddleLayer(uuid2), metrics);
        }
        fixtureNode2.setSimulation(simulation2);
        simulation2.constantSimulation(1000);
        assertEquals(fixtureNode2.getMaxId(),fixtureNode2.getCoordinatorId());

        //For all different UUID's
        MyNode fixtureNode3 = new MyNode();
        Simulator<MyNode> simulation3 = new Simulator<MyNode>(fixtureNode3, N, UnderlayType.MOCK_NETWORK);
        UUID uuid3 = simulation3.getAllId().get(0);
        for(int i = 0; i < N; i++) {
            simulation3.newInstance(UUID.randomUUID(), simulation3.getMiddleLayer(uuid3), metrics);
        }
        fixtureNode3.setSimulation(simulation3);
        simulation3.constantSimulation(1000);
        assertEquals(fixtureNode3.getMaxId(),fixtureNode3.getCoordinatorId());

    }

}


