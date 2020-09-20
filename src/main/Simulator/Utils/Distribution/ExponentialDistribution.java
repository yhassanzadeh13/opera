package Utils.Distribution;

import java.util.Random;

public class ExponentialDistribution implements BaseDistribution{

    private int mn, mx, lambda;
    private Random rand;

    public ExponentialDistribution(int mn, int mx, int lambda) {
        this.mn = mn;
        this.mx = mx;
        this.lambda = lambda;
        this.rand = new Random();
    }
    @Override
    public int next() {
        return (int) (this.mn + (this.mx - this.mn) * (Math.log(1 - rand.nextDouble()) / (-lambda)));
    }
}
