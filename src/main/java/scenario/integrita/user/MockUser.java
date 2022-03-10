package scenario.integrita.user;

/**
 * implementation of a mock user.
 */
public class MockUser implements BaseUser {
  static int readTotalTime;
  static int writeTotalTime;
  static int updateStatusTotalTime;

  static int readCount;
  static int writeCount;
  static int updateStatusCount;
  @Override
  public int read() {

    readCount ++;
    return 0;
  }

  @Override
  public int write() {

    writeCount ++;
    return 0;
  }

  @Override
  public int updateStatus() {
    updateStatusCount ++;
    return 0;
  }

  @Override
  public float getAvgReadTime() {
    return readTotalTime/readCount;
  }

  @Override
  public float getAvgWriteTime() {
    return writeTotalTime/writeCount;
  }

  @Override
  public float getAvgUpdateStatusTime() {
    return updateStatusTotalTime/updateStatusCount;
  }
}
