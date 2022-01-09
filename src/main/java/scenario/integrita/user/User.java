package scenario.integrita.user;

/**
 * contains user-related information
 */
public class User {
  public Integer id;
  public byte[] verification_key;
//  @TODO add signature verification key


  public User(Integer id) {
    this.id = id;
  }

  public User() {
  }
}
