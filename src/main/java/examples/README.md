## simulator.simulator Examples <a name="examples"></a>

Two simulator examples are provided under the `examples` package.  

### HelloServers
Simulate a basic interaction between the nodes of the servers where every 
node send "Hello" to a randomly selected node and the node replies by "Thank You".
### ServersBattle
A slightly more complicated example that illustrate the nodes setup, termination, the interaction between the events and the nodes parameters and thread safety. <br>
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
