package tools;

import cartago.*;
import jason.asSyntax.*;

/**
 * An artifact to pick the sensor with highest average trust.
 */
public class ITrustCalculator extends Artifact {
    void init() {
        // no state needed
    }

    /**
     * @OPERATION compute_best(+RatingsArray, -BestSource)
     *            RatingsArray: Object[] of Structure(SourceAtom, RateNumber)
     *            BestSource: out parameter for the Atom of the chosen sensor
     */
    @OPERATION
    void compute_best(Object[] ratingsArray, OpFeedbackParam<Term> bestSource) {
        // aggregate sum and count per sensor
        Map<String, double[]> agg = new HashMap<>();
        for (Object o : ratingsArray) {
            Structure s = (Structure) o;
            String src = s.getTerm(0).toString();
            double rate = ((NumberTerm) s.getTerm(1)).solve();
            double[] acc = agg.getOrDefault(src, new double[] { 0.0, 0.0 });
            acc[0] += rate;
            acc[1] += 1;
            agg.put(src, acc);
        }
        // pick highest average
        String winner = null;
        double bestAvg = -Double.MAX_VALUE;
        for (var e : agg.entrySet()) {
            double avg = e.getValue()[0] / e.getValue()[1];
            if (avg > bestAvg) {
                bestAvg = avg;
                winner = e.getKey();
            }
        }
        if (winner != null) {
            bestSource.set(ASSyntax.createAtom(winner));
        } else {
            // fallback, first element
            Structure s0 = (Structure) ratingsArray[0];
            bestSource.set(s0.getTerm(0));
        }
    }
}