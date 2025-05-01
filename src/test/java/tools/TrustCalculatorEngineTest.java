package tools;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TrustCalculatorEngineTest {

    @Test
    void testPickBestSensor_simple() {
        Object[] data = new Object[] {
                "structure(sensorA,1.0)",
                "structure(sensorB,2.0)",
                "structure(sensorA,3.0)"
        };
        // sensorA avg = (1.0 + 3.0) / 2 = 2.0; sensorB avg = 2.0
        // tie → first seen (sensorA) wins
        assertEquals("sensorA",
                TrustCalculatorEngine.pickBestSensor(data),
                "Should pick sensorA on tie by insertion order");
    }

    @Test
    void testPickBestSensor_clearWinner() {
        Object[] data = new Object[] {
                "structure(alpha,5)",
                "structure(beta,2)",
                "structure(alpha,3)",
                "structure(beta,4)"
        };
        // alpha avg = 4.0; beta avg = 3.0
        assertEquals("alpha",
                TrustCalculatorEngine.pickBestSensor(data),
                "alpha has higher average than beta");
    }

    @Test
    void testPickBestSensor_emptyInput() {
        Object[] data = new Object[0];
        assertNull(TrustCalculatorEngine.pickBestSensor(data),
                "Empty input should return null");
    }

    @Test
    void testPickBestSensor_ignoresMalformedStrings() {
        Object[] data = new Object[] {
                "structure(foo,1.5)",
                "not_a_structure(garbage)",
                123, // not even a String
                "structure(bar,2.5)"
        };
        // only foo and bar count: foo avg=1.5, bar avg=2.5
        assertEquals("bar",
                TrustCalculatorEngine.pickBestSensor(data),
                "Should ignore malformed entries and non-strings");
    }

    @Test
    void testPickBestSensor_multipleReadingsPerSensor() {
        Object[] data = new Object[] {
                "structure(x,1)",
                "structure(x,2)",
                "structure(x,3)",
                "structure(y,4)",
                "structure(y,6)"
        };
        // x avg = 2.0; y avg = 5.0
        assertEquals("y",
                TrustCalculatorEngine.pickBestSensor(data),
                "y should win with higher average");
    }
}