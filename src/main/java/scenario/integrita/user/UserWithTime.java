package scenario.integrita.user;

/**
 * implementation of a mock user.
 */
public class UserWithTime extends User  implements BaseUser  {
  private int readTotalTime;
  private int writeTotalTime;
  private int updateStatusTotalTime;

  private int readCount;
  private int writeCount;
  private int updateStatusCount;


  @Override
  public int read(int from, int to) {
    int total = 0;
    int updateStatusTime = updateStatus();

    total += updateStatusTime;

    readTotalTime += total;
    readCount ++;
    return total;
  }

  @Override
  public int write(int opNum) {
    int total = 0;
    int updateStatusTime = updateStatus();

    total += updateStatusTime;


    writeTotalTime += total;
    writeCount ++;
    return  0;
  }

  @Override
  public int updateStatus() {
    int total = 0;
    // 2 * Net
    total += 2 * BenchMark.NetDelay;
    total += (BenchMark.TotalServers + 3)* BenchMark.SignVerify;
    total +=  BenchMark.TotalServers * BenchMark.Fpl; // p and l depend
    updateStatusTotalTime++;
    updateStatusCount ++;

//    this.status 
    return total;
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
