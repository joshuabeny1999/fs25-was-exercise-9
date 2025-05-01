package tools;

import java.util.*;
import java.util.regex.*;

/**
 * Parses strings of the form "structure(agentName,rateValue)"
 * and picks the agent with the highest average rate.
 */
public class TrustCalculatorEngine {

    // precompile pattern: functor(args,...)
    private static final Pattern STRUCTURE = Pattern.compile(
            "^structure\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)$");

    /**
     * @param rawRatings an array of Objects, each expected to be a String
     *                   like "structure(sensing_agent_1,1)"
     * @return the agentName with the highest average rate, or null if none parsed
     */
    public static String pickBestSensor(Object[] rawRatings) {
        // preserve insertion order
        Map<String, double[]> agg = new LinkedHashMap<>();

        for (Object o : rawRatings) {
            if (!(o instanceof String))
                continue;
            String s = (String) o;
            Matcher m = STRUCTURE.matcher(s);
            if (!m.matches())
                continue;

            String agent = m.group(1);
            double rate;
            try {
                rate = Double.parseDouble(m.group(2));
            } catch (NumberFormatException ex) {
                continue;
            }

            double[] acc = agg.getOrDefault(agent, new double[] { 0.0, 0.0 });
            acc[0] += rate; // sum
            acc[1] += 1; // count
            agg.put(agent, acc);
        }

        // pick highest average
        String bestAgent = null;
        double bestAvg = -Double.MAX_VALUE;
        for (Map.Entry<String, double[]> e : agg.entrySet()) {
            double sum = e.getValue()[0];
            double cnt = e.getValue()[1];
            double avg = sum / cnt;
            if (avg > bestAvg) {
                bestAvg = avg;
                bestAgent = e.getKey();
            }
        }
        return bestAgent;
    }
}