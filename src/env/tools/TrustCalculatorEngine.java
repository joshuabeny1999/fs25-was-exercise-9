// src/env/tools/TrustCalculatorEngine.java
package tools;

import java.util.*;
import java.util.regex.*;

/**
 * Parses strings of the form "structure(agentName,rateValue)" or
 * "certified(agentName,rateValue)" and picks the agent with the highest
 * combined score: IT_CR = 0.5*IT_avg + 0.5*CR_avg.
 */
public class TrustCalculatorEngine {

    private static final Pattern STRUCTURE = Pattern.compile(
            "^interaction_trust_rating\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)");
    private static final Pattern CERTIFIED = Pattern.compile(
            "^certified_reputation_rating\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)");

    /**
     * @param rawItRatings Object[] of Strings "structure(agent,rate)"
     * @param rawCrRatings Object[] of Strings "certified(agent,rate)"
     * @return agentName with highest 0.5*IT_avg + 0.5*CR_avg
     */
    public static String pickBestByCombined(Object[] rawItRatings, Object[] rawCrRatings) {
        Map<String, double[]> itAgg = new LinkedHashMap<>();
        Map<String, double[]> crAgg = new LinkedHashMap<>();

        // parse IT ratings
        for (Object o : rawItRatings) {
            if (!(o instanceof String))
                continue;
            Matcher m = STRUCTURE.matcher((String) o);
            if (!m.matches())
                continue;
            String agent = m.group(1);
            double rate;
            try {
                rate = Double.parseDouble(m.group(2));
            } catch (NumberFormatException ex) {
                continue;
            }
            double[] acc = itAgg.getOrDefault(agent, new double[] { 0.0, 0.0 });
            acc[0] += rate;
            acc[1] += 1;
            itAgg.put(agent, acc);
        }

        // parse CR ratings
        for (Object o : rawCrRatings) {
            if (!(o instanceof String))
                continue;
            Matcher m = CERTIFIED.matcher((String) o);
            if (!m.matches())
                continue;
            String agent = m.group(1);
            double rate;
            try {
                rate = Double.parseDouble(m.group(2));
            } catch (NumberFormatException ex) {
                continue;
            }
            double[] acc = crAgg.getOrDefault(agent, new double[] { 0.0, 0.0 });
            acc[0] += rate;
            acc[1] += 1;
            crAgg.put(agent, acc);
        }

        // combine and choose
        String best = null;
        double bestScore = -Double.MAX_VALUE;
        for (String agent : itAgg.keySet()) {
            if (!crAgg.containsKey(agent))
                continue;
            double itAvg = itAgg.get(agent)[0] / itAgg.get(agent)[1];
            double crAvg = crAgg.get(agent)[0] / crAgg.get(agent)[1];
            double score = 0.5 * itAvg + 0.5 * crAvg;
            if (score > bestScore) {
                bestScore = score;
                best = agent;
            }
        }
        return best;
    }
}