package network.packets;

import java.io.Serializable;


/**
 * The Event interface is a base interface for the events that the nodes will
 * communicate with each other through.
 */

public interface Event extends Serializable {

  /**
   * Getter of the size in bytes.
   *
   * @return size returns length of payload event in bytes.
   */
  // TODO: size does not implement as number of bytes
  int size();
}
