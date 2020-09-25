package Utils.Generator;

import org.apache.commons.math3.random.JDKRandomGenerator;

public abstract class BaseGenerator {
    public int mn, mx;
    public abstract int next();
    static JDKRandomGenerator rand = new JDKRandomGenerator();
}
