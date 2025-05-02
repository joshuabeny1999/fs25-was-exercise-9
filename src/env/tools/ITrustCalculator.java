package tools;

import cartago.*;
import jason.asSyntax.*;

/**
 * CArtAgO artifact exposing compute_best_all for ASL.
 */
public class ITrustCalculator extends Artifact {
    void init() {
    }

    /**
     * @OPERATION compute_best_all(+ItArr, +CrArr, +WrArr, -Best)
     */
    @OPERATION
    void compute_best_it_cr_wr(Object[] itArr,
            Object[] crArr,
            Object[] wrArr,
            OpFeedbackParam<Term> best) {
        String winner = TrustCalculatorEngine.pickBestByIT_CR_WR(itArr, crArr, wrArr);
        if (winner != null) {
            best.set(ASSyntax.createAtom(winner));
        }
    }

    @OPERATION
    void compute_best_it_cr(Object[] itArr,
            Object[] crArr,
            OpFeedbackParam<Term> best) {
        String winner = TrustCalculatorEngine.pickBestByIT_CR(itArr, crArr);
        if (winner != null) {
            best.set(ASSyntax.createAtom(winner));
        }
    }

    @OPERATION
    void compute_best_it(Object[] itArr,
            OpFeedbackParam<Term> best) {
        String winner = TrustCalculatorEngine.pickBestByIT(itArr);
        if (winner != null) {
            best.set(ASSyntax.createAtom(winner));
        }
    }
}