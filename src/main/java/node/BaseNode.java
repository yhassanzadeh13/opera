package node;

import java.util.ArrayList;
import java.util.UUID;

import metrics.MetricsCollector;
import network.Network;
import network.packets.Event;


/**
 * The BaseNode interface is a base interface for the node class to use for the simulator.simulator.
 * onStart: to initial the node. It receives the UUID for all the nodes in the cluster.
 * onStop: is called whenever the node wants to terminate. It is used for collecting garbage
 * and possibly obtain certain log.
 * onNewMessage: is used for communication between the node itself and other nodes
 */

public interface BaseNode {


  /**
   * Is called by simulator to initialize a node with simulation parameters.
   *
   * @param identities is the list of identities of all nodes in the simulation.
   */
  void onCreate(ArrayList<Identity> identities);

  /**
   * Activated by the simulator.simulator after all the nodes in the cluster become ready
   */
  void onStart();

  /**
   * Activated by the simulator.simulator just before the node shuts down.
   */
  void onStop();

  /**
   * A channel for a node to receive a new event from other nodes.
   * The node will receive all the message through this method.
   *
   * @param originId the ID of the sender node
   * @param msg      the content of the message
   */
  void onNewMessage(UUID originId, Event msg);

  /**
   * This method serves as a factory for new node instances.
   * It is supposed to return a new instance of the special node class
   *
   * @param selfId  the ID of the new node
   * @param nameSpace string tag to virtually group the nodes (with identical tags)
   * @param network communication network for the new node.
   * @param metrics metrics collector for the node.
   * @return a new instance of the special node class.
   */
  BaseNode newInstance(UUID selfId, String nameSpace, Network network, MetricsCollector metrics);

}
