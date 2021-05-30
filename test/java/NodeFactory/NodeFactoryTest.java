package NodeFactory;

import Node.BaseNode;
import Node.NodeFactory;
import Simulator.Simulator;
import org.junit.jupiter.api.Test;
import scenario.PoV.LightChainNode;
import scenario.PoV.RegistryNode;


public class NodeFactoryTest {


    @Test
    public void messageTest(){
        NodeFactory nf = new NodeFactory();
        BaseNode node1 = new RegistryNode();
        BaseNode node2 = new LightChainNode();
        nf.put(node1, 1);
        nf.put(node2, 1);
        Simulator sim = new Simulator(nf, "mockNetwork");
        sim.constantSimulation(10);

        // how to check if messages are sent?
    }



}
