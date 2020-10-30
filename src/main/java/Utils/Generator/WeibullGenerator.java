package Utils.Generator;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class WeibullGenerator extends BaseGenerator {

    int alpha, beta;
    WeibullDistribution generator;

    public WeibullGenerator(int mn, int mx, int alpha, int beta) {
        this.mn = mn;
        this.mx = mx;
        this.alpha = alpha;
        this.beta = beta;
        this.generator = new WeibullDistribution(rand, alpha, beta, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @Override
    public int next(){
        JDKRandomGenerator generator = new JDKRandomGenerator();
        return (int) (mn + (mx - mn) * this.generator.sample());
    }
}
