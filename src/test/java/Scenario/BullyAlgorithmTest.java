package Scenario;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import SimulatorExamples.BullyAlgorithm.MyNode;
import Simulator.Simulator;

import java.util.Random;

import java.util.UUID;

public class BullyAlgorithmTest {
    /**
     * Tests the bully algorithm for only one node.
     */
    @Test
    void OneNode_Test(){
        MyNode fixtureNode = new MyNode();
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, 1, "mockNetwork");
        fixtureNode.setSimulation(simulation);
        simulation.constantSimulation(1000);
        //assertEquals(fixtureNode.getCoordinatorID(),fixtureNode.getCoordinatorID());
        assertEquals(fixtureNode.getMaxID(),fixtureNode.getCoordinatorID());
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
        Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode1, 1, "mockNetwork");
        UUID uuid = simulation.getAllID().get(0);
        simulation.newInstance(uuid,simulation.getMiddleLayer(uuid));
        fixtureNode1.setSimulation(simulation);
        simulation.constantSimulation(1000);
        assertEquals(fixtureNode1.getMaxID(),fixtureNode1.getCoordinatorID());

        //For different UUID's
        MyNode fixtureNode2 = new MyNode();
        Simulator<MyNode> simulation1 = new Simulator<MyNode>(fixtureNode2, 2, "mockNetwork");
        UUID uuid3 = simulation1.getAllID().get(0);
        simulation.newInstance(UUID.randomUUID(),simulation.getMiddleLayer(uuid3));
        fixtureNode2.setSimulation(simulation);
        simulation1.constantSimulation(1000);
        assertEquals(fixtureNode2.getMaxID(),fixtureNode2.getCoordinatorID());
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
        Simulator<MyNode> simulation1 = new Simulator<MyNode>(fixtureNode1, N, "mockNetwork");
        UUID uuid1 = simulation1.getAllID().get(0);
        for(int i = 0; i < N; i++) {
            simulation1.newInstance(uuid1, simulation1.getMiddleLayer(uuid1));
        }
        fixtureNode1.setSimulation(simulation1);
        simulation1.constantSimulation(1000);
        assertEquals(fixtureNode1.getMaxID(),fixtureNode1.getCoordinatorID());

        //For some of UUID's are same
        MyNode fixtureNode2 = new MyNode();
        Simulator<MyNode> simulation2 = new Simulator<MyNode>(fixtureNode2, N, "mockNetwork");
        UUID uuid2 = simulation2.getAllID().get(0);
        simulation2.newInstance(uuid2, simulation1.getMiddleLayer(uuid2));
        for(int i = 1; i < N; i++) {
            simulation2.newInstance(UUID.randomUUID(), simulation2.getMiddleLayer(uuid2));
        }
        fixtureNode2.setSimulation(simulation2);
        simulation2.constantSimulation(1000);
        assertEquals(fixtureNode2.getMaxID(),fixtureNode2.getCoordinatorID());

        //For all different UUID's
        MyNode fixtureNode3 = new MyNode();
        Simulator<MyNode> simulation3 = new Simulator<MyNode>(fixtureNode3, N, "mockNetwork");
        UUID uuid3 = simulation3.getAllID().get(0);
        for(int i = 0; i < N; i++) {
            simulation3.newInstance(UUID.randomUUID(), simulation3.getMiddleLayer(uuid3));
        }
        fixtureNode3.setSimulation(simulation3);
        simulation3.constantSimulation(1000);
        assertEquals(fixtureNode3.getMaxID(),fixtureNode3.getCoordinatorID());

    }

}


