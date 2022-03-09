package scenario.integrita.user;

/**
 * implementation of a mock user.
 */
public class MockUser implements BaseUser {
  @Override
  public int read() {
    return 0;
  }

  @Override
  public int write() {
    return 0;
  }

  @Override
  public int updateStatus() {
    return 0;
  }

  @Override
  public float getAvgReadTime() {
    return 0;
  }

  @Override
  public float getAvgWriteTime() {
    return 0;
  }

  @Override
  public float getAvgUpdateStatusTime() {
    return 0;
  }
}
