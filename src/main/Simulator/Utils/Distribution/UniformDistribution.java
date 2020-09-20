package Utils.Distribution;

import java.util.Random;

public class UniformDistribution implements BaseDistribution{
    int mn, mx;
    Random rand;

    public UniformDistribution(int mn, int mx){
        this.mn = mn;
        this.mx = mx;
        this.rand = new Random();
    }

    @Override
    public int next() {
        return mn + rand.nextInt(mx - mn);
    }
}
