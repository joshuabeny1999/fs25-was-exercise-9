package tools;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.*;

/**
 * Parses strings of the form
 * interaction_trust_rating(agent,rate)
 * certified_reputation_rating(agent,rate)
 * witness_reputation_rating(agent,rate)
 * and picks the agent with the highest combined score:
 * IT_CR_WR = ⅓·IT_avg + ⅓·CR_avg + ⅓·WR_avg
 */
public class TrustCalculatorEngine {

    private static final Pattern IT_STRUCT = Pattern.compile(
            "^interaction_trust_rating\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)$");
    private static final Pattern CR_STRUCT = Pattern.compile(
            "^certified_reputation_rating\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)$");
    private static final Pattern WR_STRUCT = Pattern.compile(
            "^witness_reputation_rating\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([-0-9.]+)\\s*\\)$");

    /**
     * @param rawIt Object[] of Strings "interaction_trust_rating(agent,rate)"
     * @param rawCr Object[] of Strings "certified_reputation_rating(agent,rate)"
     * @param rawWr Object[] of Strings "witness_reputation_rating(agent,rate)"
     * @return agentName with highest ⅓·IT_avg + ⅓·CR_avg + ⅓·WR_avg (or null)
     */
    public static String pickBestByIT_CR_WR(Object[] rawIt,
            Object[] rawCr,
            Object[] rawWr) {
        Map<String, double[]> itAgg = new LinkedHashMap<>();
        Map<String, double[]> crAgg = new LinkedHashMap<>();
        Map<String, double[]> wrAgg = new LinkedHashMap<>();

        // helper to parse and aggregate
        BiConsumer<Object[], Pattern> aggregate = (arr, pat) -> {
            for (Object o : arr) {
                if (!(o instanceof String))
                    continue;
                Matcher m = pat.matcher((String) o);
                if (!m.matches())
                    continue;
                String ag = m.group(1);
                double r = Double.parseDouble(m.group(2));
                Map<String, double[]> map = (pat == IT_STRUCT ? itAgg : (pat == CR_STRUCT ? crAgg : wrAgg));
                double[] acc = map.getOrDefault(ag, new double[] { 0, 0 });
                acc[0] += r;
                acc[1] += 1;
                map.put(ag, acc);
            }
        };
        aggregate.accept(rawIt, IT_STRUCT);
        aggregate.accept(rawCr, CR_STRUCT);
        aggregate.accept(rawWr, WR_STRUCT);

        String best = null;
        double bestScore = -Double.MAX_VALUE;
        // only consider agents present in all three
        for (String a : itAgg.keySet()) {
            if (!crAgg.containsKey(a) || !wrAgg.containsKey(a))
                continue;
            double itAvg = itAgg.get(a)[0] / itAgg.get(a)[1];
            double crAvg = crAgg.get(a)[0] / crAgg.get(a)[1];
            double wrAvg = wrAgg.get(a)[0] / wrAgg.get(a)[1];
            double score = (itAvg + crAvg + wrAvg) / 3.0;
            if (score > bestScore) {
                bestScore = score;
                best = a;
            }
        }
        return best;
    }

    /**
     * @param rawItRatings Object[] of Strings
     *                     "interaction_trust_rating(agent,rate)"
     * @param rawCrRatings Object[] of Strings
     *                     "certified_reputation_rating(agent,rate)"
     * @return agentName with highest 0.5*IT_avg + 0.5*CR_avg
     */
    public static String pickBestByIT_CR(Object[] rawItRatings, Object[] rawCrRatings) {
        Map<String, double[]> itAgg = new LinkedHashMap<>();
        Map<String, double[]> crAgg = new LinkedHashMap<>();

        // parse IT ratings
        for (Object o : rawItRatings) {
            if (!(o instanceof String))
                continue;
            Matcher m = IT_STRUCT.matcher((String) o);
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
            Matcher m = CR_STRUCT.matcher((String) o);
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

    /**
     * @param rawRatings an array of Objects, each expected to be a String
     *                   like "interaction_trust_rating(sensing_agent_1,1)"
     * @return the agentName with the highest average rate, or null if none parsed
     */
    public static String pickBestByIT(Object[] rawRatings) {
        // preserve insertion order
        Map<String, double[]> agg = new LinkedHashMap<>();

        for (Object o : rawRatings) {
            if (!(o instanceof String))
                continue;
            String s = (String) o;
            Matcher m = IT_STRUCT.matcher(s);
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