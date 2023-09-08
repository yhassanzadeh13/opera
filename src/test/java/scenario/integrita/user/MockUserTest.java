package scenario.integrita.user;

import org.junit.jupiter.api.Test;

public class MockUserTest {
  @Test
  public void UserWriteTest() {
    int totalObjectSize= 100;
    BaseUser u = new UserWithTime();
    for (int i = 0; i< totalObjectSize; i++){
      u.write(i);
    }
  }
}
