# distributed-simulator
distributed-simulator is an offline simulator for distributed systems. 
The Simulator provides an integratable and easy-to-use interface for running, and testing
your distributed system on various underlays, as well as extracting grafana metrics. 

## Content:
- [Architecture overview](#overview)
- [Install](#install)
- [Usage](#basic-usage)
- [Examples](#examples)
- [Documentation](#doc)

## Architecture Overview <a name="overview"></a>
The Simulator provides a channel for every node for receiving, and sending messages to the other nodes in the clsuter.
These messages are of type events, and they are used to invoke certain actions in the destination node.

## Install <a name="install"></a>
Under the master branch, you will find a Maven project which you can clone and use directly. <br>
Additionally, the simulator requires Docker to be installed on your machine. <br> 
Docker is available for free on its [official website](https://docs.docker.com/get-docker/)
## Usage <a name="basic-usage"></a>
- [Simulator setup](#setup)
- [Integrating node class](#install)
- [Integrating communication events](#basic-usage)
- [Interaction with the simulator](#basic-usage)
- [Start the simulation](#basic-usage)
- [Registering Prometheus metrics](#examples)
- [Visualizing metrics using Grafana]()
- [Supporting a new underlay](#doc)
### Simulator Setup
 load the Simulator.Simulator package to your project.
### Integrating node class
Your node class should implement `BaseNode` interface from the Simulator.Simulator package. Every node is supposed to have a unique `UUID` ID.
 which will be generated and be passed to the node by the `Simulator.Simulator`. <br> 
 Five methods needs to be overridden:
  - `onCreat`: this is where you can setup your node. All nodes' `onCreat` method will be called before any other node start processing. 
  Once the node finishes its setup it should declare itself as ready by calling `network.ready()` where `network` is an instance of `MiddleLayer`
   which will be passed by the Simulation upon creation.
  - `onStart`: to start the node's initial process. After all te nodes in the cluster are ready. the node `onStart` method will be called by the Simulator. 
  - `onStop`: this method will be called by the simulator once the node terminate. This method can be used for garbage collection.
  - `onNewMessage`: the node will receive all the event requests through this class. Every event request will be received in a separated thread.
  - `newInstance`: this method serves as a node factory method. For a given `UUID`, and a network layer `MiddleLayer`, it should return a new node instance.
  
### Integrating communication events
All the event classes in the network should implement the `Event` interface from the `Simulator.Simulator` package.
 You will need these events to send messages between nodes through the Simulator 
Two methods should be implemented.
  - `actionPerformed`: receive an instance from the host node that will perform the event and will be used to activate the event by the user. 
  Once the destination node receive the event, it can activate the action by calling `event.actionPerformed(this)`
  - `logMessage`: should return a message of the event state. It is used for the logging purpose.
  
### Interaction with the simulator
The simulator provides a simulated network underlay for the sake of the nodes' communication. 
It provides the following methods:
  - `network.ready`: for the node to declare itself as ready after it finishes its setup.
  - `network.send(BaseNode targetNode, Event event)`: is used to send an event from one node to another.  
  - `network.done`: can be used for the node to terminate itself. The simulator will delete this node from the network and call the node `onStop` method.

Simulator.Simulator static logger can also be accessed using `Simulator.Simulator.getLogger()`
  
### Start the simulation
Consider you have a `myNode` class, and you want to run a simulation of **100** nodes. <br>
you need to create a new `Simulator` instance and pass a fixture factory node, and the number of nodes in the simulation. 
Subsequently, you can either start a constant simulation using `constantSimulation(duration)` or start
a simulation with churn feature using `churnSimulation(Long sessionLength, BaseGenerator sessionLength, BaseGenerator InterArrivalTime)`.
 <br> Various types of distributions can be accessed from the Generator package.

```
myNode fixtureNode = new myNode();
Simulator<myNode> simulation = new Simulator<myNode>(fixtureNode, 5, "tcp");

simulation.constantSimulation(10000);

simulation.churnSimulation(10000, new UniformGenerator(1000, 3000),
        new WeibullGenerator(1000, 3000, 1, 4));
```

The output log of the simulation will be generated in a `log.out` file under your project's directory.  
  
### Registering Prometheus metrics
The simulator provides three metric types under the `Metric` package-- `SimulatorCounter`, `SimulatorGauge`, and `SimulatorHistogram` <br>
the static `register` method can be used to register a new metric:
```
SimulatorCounter.register("MetricName")
```
The Simulator provides basic default metrics such as 
packets delay, number of sent messages, number of received messages, session length, inter-arrival time.<br>

### Visualizing Simulator metrics in Grafana
The Simulator uses a Docker container for Prometheus and configures it automatically with Grafana.
You can directly access Prometheus on `localhost:9090`, and Grafana on `localhost:3030`. 

#### Example of visualizing the default metrics
Access Grafana on `localhost:3030`. The default username and password is `admin`.
Create a new dashboard, and add a new panel. 

 

## Simulation Examples <a name="examples"></a>

Two simulation examples are provided under the `SimulatorExamples` package.  

### HelloServers
Simulate a basic interaction between the nodes of the servers where every 
node send "Hello" to a randomly selected node, and the node replies by "Thank You".
### ServersBattle
A slightly more complicated example that illustrate the nodes' setup, termination, the interaction between the events and the nodes parameters and thread safety. <br>
It demonstrates a battle between the servers.
- Every node start with an initial random power level. 
- Every node sends a `BattleInvitation` of a battle with a random duration to a randomly selected node.
- This node either confirm the battle or decline it (in case it was involved in another fight) by sending an `BattleConfirmation` event.
- In case of confirmation, the host node either starts the fight or abort the game in case it was involved in another fight while the invitation was pending.
- In case the fight started, the node with the higher power level win. It gains +5 points for winning, -10 for losing, and 1 for drawing. 
It let the opponent node knows the results by sending `BattleResult` event.
- When a node reaches a zero power level, it dies and send a goodbye message. 
The simulation continues until either the simulation duration finishes, or a winner (a single node) is declared.
- Prometheus' metrics for the fight duration, health level, and number of fights are provided for each node.

## Documentation <a name="doc"></a>
the JavaDoc Documentation can be found under the `doc` directory under the project directory.