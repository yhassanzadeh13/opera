package node;

import java.util.ArrayList;

import network.model.Event;


/**
 * Any node implementation should implement this interface.
 */

public interface BaseNode {


  /**
   * This method is called when a new node is created. It is used to initialize the node.
   *
   * @param allId the identifier of all nodes in the simulation. This includes the current node.
   */
  void onCreate(ArrayList<Identifier> allId);

  /**
   * This method is called when all nodes involved in the simulation have been created, and are ready to start.
   * It is used to start the node.
   */
  void onStart();

  /**
   * This method is called when the node wants to terminate. It is used to collect garbage and possibly obtain certain log.
   */
  void onStop();

  /**
   * This method is called when a new message is received by the node.
   *
   * @param originId the ID of the sender node
   * @param msg      the content of the message
   */
  void onNewMessage(Identifier originId, Event msg);

  /**
   * Creates a new instance of the node.
   *
   * @param selfId    the ID of the new node
   * @param nameSpace string tag to virtually group the nodes (with identical tags)
   * @param network   communication network for the new node.
   * @return a new instance of the special node class.
   */
  BaseNode newInstance(Identifier selfId, String nameSpace, network.Network network);

}
