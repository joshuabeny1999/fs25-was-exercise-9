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
    void compute_best_cr(Object[] rawItRatings, Object[] rawCrRatings, OpFeedbackParam<Term> bestSource) {
        String winner = TrustCalculatorEngine.pickBestByCombined(rawItRatings, rawCrRatings);
        if (winner != null) {
            bestSource.set(ASSyntax.createAtom(winner));
        }
    }
}