package scenario.integrita.user;

import scenario.integrita.historytree.NodeAddress;

/**
 * contains user-related information.
 */
public class User {
  public Integer id;
  public byte[] vk; // signature verification key
  public NodeAddress status;

  // constructors ------------------
  public User(Integer id) {
    this.id = id;
  }

  public User() {
  }

  public User(Integer id, byte[] vk) {
    this.id = id;
    this.vk = vk;
  }
}
