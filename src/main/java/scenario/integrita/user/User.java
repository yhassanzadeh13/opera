package scenario.integrita.user;

/**
 * contains user-related information.
 */
public class User {
  public Integer id;
  public byte[] vk;

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
