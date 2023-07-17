package scenario.integrita;

import network.NetworkProtocol;
import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;

/**
 * Integrita simulation.
 */
public class Main {

    /**
     * spins up client and server nodes .
     * clients nodes have access to a shared data.
     * the shared data is stored by the servers side in a distributed fashion.
     * clients make read and write requests to the servers using Integrita algorithms.
     *
     * @param args not needed.
     */
    public static void main(String[] args) {

        final String nameSpace = "integrita";
        Factory factory = new Factory();
        factory.addRecipe(new Recipe(new Client(), nameSpace, 1));
        factory.addRecipe(new Recipe(new Server(), nameSpace, 1));

        Simulator simulator = new Simulator(factory, NetworkProtocol.MOCK_NETWORK);
        simulator.constantSimulation(10000);
    }
}
