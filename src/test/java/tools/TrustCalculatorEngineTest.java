package tools;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TrustCalculatorEngineTest {

    @Test
    void testAll_tie() {
        Object[] its = { "interaction_trust_rating(a,1)", "interaction_trust_rating(b,1)" };
        Object[] crs = { "certified_reputation_rating(a,0)", "certified_reputation_rating(b,2)" };
        Object[] wrs = { "witness_reputation_rating(a,1)", "witness_reputation_rating(b,1)" };
        // a: (1 + 0 + 1)/3 = 0.67, b: (1 + 2 +1)/3 = 1.33 → b wins
        assertEquals("b", TrustCalculatorEngine.pickBestByIT_CR_WR(its, crs, wrs));
    }

    @Test
    void testAll_missingWR() {
        Object[] its = { "interaction_trust_rating(x,2)" };
        Object[] crs = { "certified_reputation_rating(x,1)" };
        Object[] wrs = {}; // no witness ratings
        assertNull(TrustCalculatorEngine.pickBestByIT_CR_WR(its, crs, wrs));
    }

    @Test
    void testIR_CR_simple() {
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
                TrustCalculatorEngine.pickBestByIT_CR(its, crs));
    }

    @Test
    void testIR_CR_clearWinner() {
        Object[] its = new Object[] {
                "interaction_trust_rating(x,0)", "interaction_trust_rating(y,2)"
        };
        Object[] crs = new Object[] {
                "certified_reputation_rating(x,1)", "certified_reputation_rating(y,1)"
        };
        // x: (0+1)/2=0.5; y: (2+1)/2=1.5
        assertEquals("y",
                TrustCalculatorEngine.pickBestByIT_CR(its, crs));
    }

    @Test
    void testIR_CR_missingCR() {
        Object[] its = new Object[] {
                "interaction_trust_rating(a,1)"
        };
        Object[] crs = new Object[] { /* no certified_reputation_rating(a,...) */ };
        assertNull(
                TrustCalculatorEngine.pickBestByIT_CR(its, crs));
    }

    @Test
    void testIR_CR_ignoreBadFormats() {
        Object[] its = new Object[] {
                "interaction_trust_rating(p,1)", "badformat"
        };
        Object[] crs = new Object[] {
                "certified_reputation_rating(p,1)", 1234
        };
        assertEquals("p",
                TrustCalculatorEngine.pickBestByIT_CR(its, crs));
    }

    @Test
    void testPickBestSensorIT_simple() {
        Object[] data = new Object[] {
                "interaction_trust_rating(sensorA,1.0)",
                "interaction_trust_rating(sensorB,2.0)",
                "interaction_trust_rating(sensorA,3.0)"
        };
        // sensorA avg = (1.0 + 3.0) / 2 = 2.0; sensorB avg = 2.0
        // tie → first seen (sensorA) wins
        assertEquals("sensorA",
                TrustCalculatorEngine.pickBestByIT(data),
                "Should pick sensorA on tie by insertion order");
    }

    @Test
    void testPickBestSensorIT_clearWinner() {
        Object[] data = new Object[] {
                "interaction_trust_rating(alpha,5)",
                "interaction_trust_rating(beta,2)",
                "interaction_trust_rating(alpha,3)",
                "interaction_trust_rating(beta,4)"
        };
        // alpha avg = 4.0; beta avg = 3.0
        assertEquals("alpha",
                TrustCalculatorEngine.pickBestByIT(data),
                "alpha has higher average than beta");
    }

    @Test
    void testPickBestSensorIT_emptyInput() {
        Object[] data = new Object[0];
        assertNull(TrustCalculatorEngine.pickBestByIT(data),
                "Empty input should return null");
    }

    @Test
    void testPickBestSensorIT_ignoresMalformedStrings() {
        Object[] data = new Object[] {
                "interaction_trust_rating(foo,1.5)",
                "not_a_structure(garbage)",
                123, // not even a String
                "interaction_trust_rating(bar,2.5)"
        };
        // only foo and bar count: foo avg=1.5, bar avg=2.5
        assertEquals("bar",
                TrustCalculatorEngine.pickBestByIT(data),
                "Should ignore malformed entries and non-strings");
    }

    @Test
    void testPickBestSensorIT_multipleReadingsPerSensor() {
        Object[] data = new Object[] {
                "interaction_trust_rating(x,1)",
                "interaction_trust_rating(x,2)",
                "interaction_trust_rating(x,3)",
                "interaction_trust_rating(y,4)",
                "interaction_trust_rating(y,6)"
        };
        // x avg = 2.0; y avg = 5.0
        assertEquals("y",
                TrustCalculatorEngine.pickBestByIT(data),
                "y should win with higher average");
    }
}