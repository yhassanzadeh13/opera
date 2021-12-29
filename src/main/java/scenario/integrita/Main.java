package scenario.integrita;

import simulator.Factory;
import simulator.Recipe;
import simulator.Simulator;
import underlay.UnderlayType;

public class Main {
    public static void main(String[] args) {
        /**
         *
         */
        final String nameSpace = "integrita";
        Factory factory = new Factory();
        factory.addRecipe(new Recipe(new Client(), nameSpace, 1));
        factory.addRecipe(new Recipe(new Server(), nameSpace, 1));

        Simulator simulator = new Simulator(factory, UnderlayType.MOCK_NETWORK);
        simulator.constantSimulation(10000);
    }
}
