package tools;

import cartago.*;
import jason.asSyntax.*;

public class ITrustCalculator extends Artifact {
    void init() {
    }

    /**
     * @OPERATION compute_best(+RawRatingsArray, -BestSource)
     *            RawRatingsArray: Object[] of Strings like "structure(agent,rate)"
     */
    @OPERATION
    void compute_best(Object[] rawRatings, OpFeedbackParam<Term> bestSource) {
        String winner = TrustCalculatorEngine.pickBestSensor(rawRatings);
        if (winner != null) {
            bestSource.set(ASSyntax.createAtom(winner));
        }
    }
}