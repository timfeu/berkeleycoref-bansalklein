package feature_test.pronoun;

import edu.berkeley.nlp.util.Logger;

public class FeaturePronounReplacementCounts {
    ExtractPronounReplacementCounts extractor;

    public FeaturePronounReplacementCounts(ExtractPronounReplacementCounts extractor) {
        this.extractor = extractor;
    }

    public String[] getFeature(String line) {
        String[] features = new String[3]; // r1, r2 and r1gap feature values

        String[] tokens = line.trim().split(" ");
        assert tokens.length == 5 : "Wrong format - should have two headwords followed by context and isPossessive value 5";

        String pronoun = tokens[0];
        String replacement = tokens[1];
        String r1 = tokens[2];
        String r2 = tokens[3];
        boolean isPossessive = Boolean.valueOf(tokens[4]);

        // R1 feature
        if (ExtractPronounReplacementCounts.PLACEHOLDER.equals(r1)) {
            features[0] = "M2";
        } else {
            if (isPossessive) {
                features[0] = calculate(String.format("%s 's %s", replacement, r1), String.format("<*> 's %s", r1),
                        replacement);
            } else {
                features[0] = calculate(String.format("%s %s", replacement, r1), String.format("<*> %s", r1),
                        replacement);
            }
        }

        if (ExtractPronounReplacementCounts.PLACEHOLDER.equals(r1) || ExtractPronounReplacementCounts.PLACEHOLDER.equals(r2)) {
            features[1] = "M2";
            features[2] = "M2";
        } else {
            if (isPossessive) {
                // R2 feature
                features[1] = calculate(String.format("%s 's %s %s", replacement, r1, r2), String.format("<*> 's %s %s", r1, r2), replacement);

                // R1 gap feature
                features[2] = calculate(String.format("%s 's <*> %s", replacement, r2), String.format("<*> 's <*> %s", r2), replacement);
            } else {
                // R2 feature
                features[1] = calculate(String.format("%s %s %s", replacement, r1, r2), String.format("<*> %s %s", r1, r2), replacement);

                // R1 gap feature
                features[2] = calculate(String.format("%s <*> %s", replacement, r2), String.format("<*> <*> %s", r2), replacement);
            }
        }

        assert(features[0] != null);
        assert(features[1] != null);
        assert(features[2] != null);

        return features;
    }

    private String calculate(String fullCountPattern, String normalizedCountPattern, String replacement) {
        assert(extractor.contextCounts.containsKey(fullCountPattern));
        assert(extractor.contextCounts.containsKey(normalizedCountPattern));
        assert(extractor.unigramCounts.containsKey(replacement));

        double fullCount = extractor.contextCounts.getCount(fullCountPattern);
        double normalizedCount = extractor.contextCounts.getCount(normalizedCountPattern);
        double headwordCount = extractor.unigramCounts.getCount(replacement);

        assert(normalizedCount >= fullCount);

        if (fullCount == 0 || normalizedCount == 0 || headwordCount == 0) {
            return "M1";
        } else {
            double cnt_ans1 = Math.log10(fullCount) - Math.log10(normalizedCount) - Math.log10(headwordCount);  //log[cnt(A,B)/cnt(A)*cnt(B)
            return String.valueOf(cnt_ans1);
        }
    }
}
