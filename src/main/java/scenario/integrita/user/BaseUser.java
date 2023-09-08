package scenario.integrita.user;

/**
 * defines necessary interface for Integrita user.
 */
public interface BaseUser {

  /**
   * Integrita read protocol.
   *
   * @return read run time in milliseconds
   */
  int read(int from, int to);

  /**
   * Integrita Write protocol.
   *
   * @return Write runtime in milliseconds
   */
  int write(int opNum);

  /**
   * Integrita UpdateStatus protocol.
   *
   * @return UpdateStatus runtime in milliseconds
   */
  int updateStatus();

  /**
   * calculates the average running time of the read protocol.
   *
   * @return average Read runtime in milliseconds
   */
  float getAvgReadTime();

  /**
   * calculates the average running time of the write protocol.
   *
   * @return aßverage Write runtime in milliseconds
   */
  float getAvgWriteTime();

  /**
   * calculates the average running time of the UpdateStatus protocol.
   *
   * @return average UpdateStatus runtime in milliseconds
   */
  float getAvgUpdateStatusTime();
}
