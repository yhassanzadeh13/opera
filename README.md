# distributed-simulator
distributed-simulator is an offline thread-based simulator for distributed systems.

## Content:
- [install](#install)
- [Basic usage](#basic-usage)
- [Examples](#examples)
- [Documentation](#doc)


## install <a name="install"></a>
Under the master branch, you will find a Maven project which you can clone and use directly. <br>
Alternatively, You can load the Simulator package to your own project, and 
include the `log4j` dependencies. <br>

```
<dependencies>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
    </dependencies>
```
## Usage <a name="basic-usage"></a>
### Setup
 load the Simulator package to your project
### Node Class
Your special `Node` class should implement `BaseNode` interface from the Simulator package. Every node is supposed to have a unique `UUID` ID.
 which will be generated and be passed to the node by the `Simulator`. <br> 
 Five methods needs to be overridden
  - `onCreat` to initialize the node setup. All the nodes `onCreat` methods will be called before any node start processing. 
  Once the node finishes its setup it should declare itself as ready by calling `Simulator.Ready(nodeId)` 
  - `onStart` to start the node's initial process. After all te nodes in the cluster are ready. the node `onStart` method will be called by the `Simulator` 
  - `onStop` this method will be called by the simulator once the node terminate. This method can be used for garbage collection.
  - `onNewMessage` the node will receive all the event requests through this class. Every event request will be received in a separated thread.
  - `newInstance` this method serves as a node factory method. For a given `UUID`, it should return a new node instance.
  
### Event Class
All the event classes in the network should implement the `Event` interface from the `Simulator` package.
Two methods methods be overridden
  - `actionPerformed` receive an instance from the host node that will perform the event and will be used to activate the event by the user.
  - `logMessage` should return a message of the event state. It is used for the logging purpose.
  
### Interaction with the simulator
The simulator provides a simulated network underlay for the sake of the nodes' communication. <br>
Four main static methods are provided.
  - `Simulator.Ready` for the node to declare itself as ready after if finished its setup.
  - `Simulator.Submit`can be used to send a message from one node to another. It receives the sender `UUID`, the target `UUID` and an event. 
  - `Simulator.Done` can be used for the node to terminate itself. The simulator will delete this node from the network and call the node `onStop` method.
  - `Simulator.getLogger` can be used to log a new message.
  
### Start new simulation
Consider you have a `myNode` class and you want to run a simulation of **100** nodes. <br>
you need to create a new `Simulator` instance and pass a dummy factory node and the number of nodes in the simulation. 
To start the simulation call `.start()` method and pass the simulation duration is millisecond.

```
Simulator<myNode> simulation = new Simulator<myNode>(new myNode(UUID.randomUUID()), 100);
simulation.start(10000);
```

The output log of the simulation in the `log.out` file under you project directory.  
  
## Simulator Examples <a name="examples"></a>

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

## Documentation <a name="doc"></a>
the JavaDoc Documentation can be found under the `doc` directory under the project directory.