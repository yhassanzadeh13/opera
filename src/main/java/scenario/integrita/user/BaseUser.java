package scenario.integrita.user;

public interface BaseUser {

  /**
   *
   * @return read run time in milliseconds
   */
  int Read();

  /**
   *
   * @return Write runtime in milliseconds
   */
  int Write();

  /**
   *
   * @return UpdateStatus runtime in milliseconds
   */
  int UpdateStatus();

  /**
   *
   * @return average Read runtime in milliseconds
   */
  float getAvgReadTime();

  /**
   *
   * @return average Write runtime in milliseconds
   */
  float getAvgWriteTime();

  /**
   *
   * @return average UpdateStatus runtime in milliseconds
   */
  float getAvgUpdateStatusTime();
}
