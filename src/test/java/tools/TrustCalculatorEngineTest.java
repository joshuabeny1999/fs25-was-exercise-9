package tools;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TrustCalculatorEngineTest {

    @Test
    void testCombined_simple() {
        Object[] its = new Object[] {
                "interaction_trust_rating(a,1)", "interaction_trust_rating(b,0), interaction_trust_rating(a,1)"
        };
        Object[] crs = new Object[] {
                "certified_reputation_rating(a,0)", "certified_reputation_rating(b,1)"
        };
        // a: IT_avg=(1+1)/2=1, CR_avg=0 => score=0.5; b: IT_avg=0, CR_avg=1 =>
        // score=0.5;
        // tie by insertion => a
        assertEquals("a",
                TrustCalculatorEngine.pickBestByCombined(its, crs));
    }

    @Test
    void testCombined_clearWinner() {
        Object[] its = new Object[] {
                "interaction_trust_rating(x,0)", "interaction_trust_rating(y,2)"
        };
        Object[] crs = new Object[] {
                "certified_reputation_rating(x,1)", "certified_reputation_rating(y,1)"
        };
        // x: (0+1)/2=0.5; y: (2+1)/2=1.5
        assertEquals("y",
                TrustCalculatorEngine.pickBestByCombined(its, crs));
    }

    @Test
    void testCombined_missingCR() {
        Object[] its = new Object[] {
                "interaction_trust_rating(a,1)"
        };
        Object[] crs = new Object[] { /* no certified_reputation_rating(a,...) */ };
        assertNull(
                TrustCalculatorEngine.pickBestByCombined(its, crs));
    }

    @Test
    void testCombined_ignoreBadFormats() {
        Object[] its = new Object[] {
                "interaction_trust_rating(p,1)", "badformat"
        };
        Object[] crs = new Object[] {
                "certified_reputation_rating(p,1)", 1234
        };
        assertEquals("p",
                TrustCalculatorEngine.pickBestByCombined(its, crs));
    }

}
