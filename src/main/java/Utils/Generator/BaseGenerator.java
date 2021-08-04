package utils.generator;

import org.apache.commons.math3.random.JDKRandomGenerator;

public abstract class BaseGenerator {
  public int mn;
  public int mx;

  public abstract int next();

  static JDKRandomGenerator rand = new JDKRandomGenerator();
}
