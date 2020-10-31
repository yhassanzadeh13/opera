# distributed-simulator
distributed-simulator is an offline thread-based simulator for distributed systems.

## Content:
- [install](#install)
- [Basic usage](#basic-usage)
- [Examples](#examples)
- [Documentation](#doc)


## install <a name="install"></a>
Under the master branch, you will find a Maven project which you can clone and use directly. <br>
Alternatively, You can load the Simulator.Simulator package to your own project, and 
include the `log4j`, `YAML`, and `promotheus` dependencies. <br>

```
<dependencies>
        <!-- log4j -->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
        <!-- YAML -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>1.21</version>
        </dependency>
        <!-- The client -->
        <dependency>
            <groupId>io.prometheus</groupId>
            <artifactId>simpleclient</artifactId>
            <version>0.9.0</version>
        </dependency>
        <!-- Hotspot JVM metrics-->
        <dependency>
            <groupId>io.prometheus</groupId>
            <artifactId>simpleclient_hotspot</artifactId>
            <version>0.9.0</version>
        </dependency>
        <!-- Exposition HTTPServer-->
        <dependency>
            <groupId>io.prometheus</groupId>
            <artifactId>simpleclient_httpserver</artifactId>
            <version>0.9.0</version>
        </dependency>
    </dependencies>
```
## Usage <a name="basic-usage"></a>
### Setup
 load the Simulator.Simulator package to your project
### Node Class
Your `Node` class should implement `BaseNode` interface from the Simulator.Simulator package. Every node is supposed to have a unique `UUID` ID.
 which will be generated and be passed to the node by the `Simulator.Simulator`. <br> 
 Five methods needs to be implemented
  - `onCreat` to initialize the node setup. All nodes' `onCreat` method will be called before any other node start processing. 
  Once the node finishes its setup it should declare itself as ready by calling `Simulator.Simulator.Ready(nodeId)` 
  - `onStart` to start the node's initial process. After all te nodes in the cluster are ready. the node `onStart` method will be called by the `Simulator.Simulator` 
  - `onStop` this method will be called by the simulator once the node terminate. This method can be used for garbage collection.
  - `onNewMessage` the node will receive all the event requests through this class. Every event request will be received in a separated thread.
  - `newInstance` this method serves as a node factory method. For a given `UUID`, and a network layer `MiddleLayer`, it should return a new node instance.
  
### Event Class
All the event classes in the network should implement the `Event` interface from the `Simulator.Simulator` package.
Two methods should be impelemented
  - `actionPerformed` receive an instance from the host node that will perform the event and will be used to activate the event by the user.
  - `logMessage` should return a message of the event state. It is used for the logging purpose.
  
### Interaction with the simulator
The simulator provides a simulated network Underlay for the sake of the nodes' communication. <br>
Through the network layer 'MiddleLayer', provided on the signature of the 'newInstance' method in the `BaseNode` interface, four basic methods are provided.
  - `network.ready` for the node to declare itself as ready after if finished its setup.
  - `network.send`can be used to send a message from one node to another. It receives the target `UUID` and an event. 
  - `network.done` can be used for the node to terminate itself. The simulator will delete this node from the network and call the node `onStop` method.

Simulator.Simulator static logger can also be accessed using `Simulator.Simulator.getLogger()`
  
### Start new simulation
Consider you have a `myNode` class and you want to run a simulation of **100** nodes. <br>
you need to create a new `Simulator.Simulator` instance and pass a fixture factory node, and the number of nodes in the simulation. 
You can either start a constant simulation by calling `constantSimulation(duration)` or start
a simulation with churn feature by calling `churnSimulation()` and pass the session, and 
inter-arrival time generators from the Generator package.

```
myNode fixtureNode = new myNode();
Simulator.Simulator<myNode> simulation = new Simulator.Simulator<myNode>(fixtureNode, 5, "tcp");

simulation.constantSimulation(10000);

simulation.churnSimulation(10000, new UniformGenerator(1000, 3000),
        new WeibullGenerator(1000, 3000, 1, 4));
```

The output log of the simulation in the `log.out` file under you project's directory.  
  
### Extracting and registering Prometheus metrics
The simulator provides three metric types under the `Metric` package-- `SimulatorCounter`, `SimulatorGauge`, and `SimulatorHistogram` <br>
In order to register a new metric, call the static `register` method, and provide the metric name
```
SimulatorCounter.register("MetricName")
```
For every metric type, update static methods are provided. They can be called by providing
the metric name, and the node UUID.<br>
For every node, the simulator provides three basic metrics-- packets delay, number of sent messages, number of received messages.<br>
prometheus is available in `Homebrew` for Unix systems
```
brew install prometheus
``` 
In order to run prometheus configured with the simulator, use the provided `prometheus.yml` config file
```
sudo prometheus --config.file=prometheus.yml
```

## Simulator.Simulator Examples <a name="examples"></a>

Two simulator examples are provided under the `SimulatorExamples` package.  

### HelloServers
Simulate a basic interaction between the nodes of the servers where every 
node send "Hello" to a randomly selected node and the node replies by "Thank You".
### ServersBattle
A slightly more complicated example that illustrate the nodes setup, termination, the interaction between the events and the nodes parameters and thread safety. <br>
It demonstrate a battle between the servers.
- Every node start with an initial random power level. 
- Every node send a `BattleInvitation` of a battle with a random duration to a randomly selected node.
- This node either confirm the battle or decline it (in case it was involved in another fight) by sending an `BattleConfirmation` event.
- In case of confirmation, the host node either starts the fight or abort the game in case it was involved in another fight while the invitation was pending.
- In case the fight started, the node with the higher power level win. It gains +5 points for winning, -10 for losing, and 1 for drawing. 
It let the opponent node knows the results by sending `BattleResult` event.
- When a node reaches a zero power level, it dies and send a goodbye message. 
The simulation continues until either the simulation duration finishes or a winner (a single node) is declared.
- Prometheus metrics for the fight duration, health level, and number of fights are provided for each node.

## Documentation <a name="doc"></a>
the JavaDoc Documentation can be found under the `doc` directory under the project directory.