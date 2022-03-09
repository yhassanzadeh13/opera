package scenario.integrita.user;

public class MockUser implements BaseUser{
  @Override
  public int Read() {
    return 0;
  }

  @Override
  public int Write() {
    return 0;
  }

  @Override
  public int UpdateStatus() {
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
