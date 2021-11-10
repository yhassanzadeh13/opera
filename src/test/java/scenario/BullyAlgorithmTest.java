package scenario;

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
   * For the single node system coordinator is the node itself.
   * Therefore, coordinatorID should be equal to the node's Id.
   */
  @Test
  void OneNode_Test(){
    MyNode fixtureNode = new MyNode();
    Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode, 1, UnderlayType.MOCK_NETWORK);
    simulation.constantSimulation(1000);
    //assertEquals(fixtureNode.getCoordinatorID(),fixtureNode.getCoordinatorID());
    assertEquals(fixtureNode.getMaxId(),fixtureNode.getCoordinatorId());
  }

  /**
   * Tests the bully algorithm for two nodes with two cases:
   * Case1- Nodes have same UUID
   * In the test with same UUID coordinator is the first node that sends the message.
   * Case2- Nodes have different UUID
   * In the test with different UUID coordinator is the node with the bigger UUID.
   */
  @Test
  void TwoNode_Test(){
    //For same UUID's
    MyNode fixtureNode1 = new MyNode();
    Simulator<MyNode> simulation = new Simulator<MyNode>(fixtureNode1, 1, UnderlayType.MOCK_NETWORK);
    UUID uuid = simulation.getAllId().get(0);
    simulation.newInstance(uuid,simulation.getMiddleLayer(uuid),metrics);
    simulation.constantSimulation(1000);
    assertEquals(fixtureNode1.getMaxId(),fixtureNode1.getCoordinatorId());

    //For different UUID's
    MyNode fixtureNode2 = new MyNode();
    Simulator<MyNode> simulation1 = new Simulator<MyNode>(fixtureNode2, 2, UnderlayType.MOCK_NETWORK);
    UUID uuid3 = simulation1.getAllId().get(0);
    simulation.newInstance(UUID.randomUUID(),simulation.getMiddleLayer(uuid3),metrics);
    simulation1.constantSimulation(1000);
    assertEquals(fixtureNode2.getMaxId(),fixtureNode2.getCoordinatorId());
  }

  /**
   * Tests the bully algorithm for random number of nodes in 3 cases:
   * Case1- All nodes have same UUID
   * In the test with same UUID coordinator is the first node that sends the message.
   * Case2- Some nodes have same UUID
   * In the test with some same UUID's if the biggest UUID is unique then node with the biggest UUID is the coordinator.
   * If the biggest UUID is not unique the node which sends the victory message first is the coordinator.
   * Case3- All nodes have different UUID
   * In the test with different UUID coordinator is the node with the biggest UUID.
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
    simulation2.constantSimulation(1000);
    assertEquals(fixtureNode2.getMaxId(),fixtureNode2.getCoordinatorId());

    //For all different UUID's
    MyNode fixtureNode3 = new MyNode();
    Simulator<MyNode> simulation3 = new Simulator<MyNode>(fixtureNode3, N, UnderlayType.MOCK_NETWORK);
    UUID uuid3 = simulation3.getAllId().get(0);
    for(int i = 0; i < N; i++) {
      simulation3.newInstance(UUID.randomUUID(), simulation3.getMiddleLayer(uuid3), metrics);
    }
    simulation3.constantSimulation(1000);
    assertEquals(fixtureNode3.getMaxId(),fixtureNode3.getCoordinatorId());

  }

}


