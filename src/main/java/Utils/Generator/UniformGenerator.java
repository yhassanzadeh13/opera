package Utils.Generator;

public class UniformGenerator extends BaseGenerator {
    public UniformGenerator(int mn, int mx){
        this.mn = mn;
        this.mx = mx;
    }

    @Override
    public int next() {
        return mn + rand.nextInt(mx - mn);
    }
}
