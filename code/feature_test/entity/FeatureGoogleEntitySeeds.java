package feature_test.entity;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FeatureGoogleEntitySeeds {
    private final ExtractGoogleEntitySeeds extractor;
    private final MaxentTagger tagger;
    int[] topSimpleSeeds;
    int[] topPosSeeds;

    public FeatureGoogleEntitySeeds(ExtractGoogleEntitySeeds extractor, String modelFile, int[] topSimpleSeeds, int[]
            topPosSeeds) {
        this.extractor = extractor;
        this.topPosSeeds = topPosSeeds;
        this.topSimpleSeeds = topSimpleSeeds;

        assert topSimpleSeeds.length > 0;
        assert topPosSeeds.length > 0;
        assert containsOnlyUniqueElements(topSimpleSeeds);
        assert containsOnlyUniqueElements(topPosSeeds);
        assert isSorted(topSimpleSeeds);
        assert isSorted(topPosSeeds);

        Logger.logss("Loading tagger...");
        tagger = new MaxentTagger(modelFile);
        Logger.logss("[done]");
    }

    private boolean isSorted(int[] array) {
        int last = array[0];
        for (int x : array) {
            if (x < last) return false;
            last = x;
        }
        return true;
    }

    private boolean containsOnlyUniqueElements(int[] elements) {
        HashSet<Integer> set = new HashSet<>();
        for (int element : elements) {
            if (set.contains(element)) {
                return false;
            }
            set.add(element);
        }
        return true;
    }

    public String[] getSimpleSeedsValues(String h1, String h2) {
        List<String> seedList1 = extractor.seeds.get(h1);
        List<String> seedList2 = extractor.seeds.get(h2);

        String[] featureValues = new String[topSimpleSeeds.length];

        if (seedList1 == null || seedList2 == null) {
            Arrays.fill(featureValues, "false");
            return featureValues;
        }

        HashSet<String> set1 = new HashSet<>();

        int currentSeedIdx = 0;
        for (int kPointer = 0; kPointer < topSimpleSeeds.length; kPointer++) {
            int k = topSimpleSeeds[kPointer];
            while (currentSeedIdx < k) {
                if (currentSeedIdx >= seedList1.size()) {
                    break;
                }
                set1.add(seedList1.get(currentSeedIdx));
                currentSeedIdx++;
            }

            for (int i = 0; i < k; i++) {
                if (i >= seedList2.size()) {
                    break;
                }
                if (set1.contains(seedList2.get(i))) {
                    // we have found a match for < k_current; all k_n > k_current therefore also have a match
                    for (int v = kPointer; v < topSimpleSeeds.length; v++) {
                        featureValues[v] = "true";
                    }
                    return featureValues;
                }
            }
            // no match found
            featureValues[kPointer] = "false";
        }

        return featureValues;
    }

    HashSet<String> wantedPos = new HashSet<>(Arrays.asList("JJ", "NN", "RB", "VB"));

    public String[] getPosSeedValues(String h1, String h2) {
        List<String> seedList1 = extractor.seeds.get(h1);
        List<String> seedList2 = extractor.seeds.get(h2);

        String[] featureValues = new String[topPosSeeds.length];

        for (int kPointer = 0; kPointer < topPosSeeds.length; kPointer++) {
            int k = topPosSeeds[kPointer];
            Counter<String> matchPos = getIntersectionPos(seedList1, seedList2, k);

            String maxPos = "M1";
            for (String key : matchPos.getSortedKeys()) {
                if (wantedPos.contains(key)) {
                    maxPos = key;
                    break;
                }
            }
            featureValues[kPointer] = maxPos;
        }

        return featureValues;
    }

    private Counter<String> getIntersectionPos(List<String> seedList1, List<String> seedList2, int k) {
        HashSet<String> set1 = new HashSet<>();
        Counter<String> counter = new Counter<>();

        for (int i = 0; i < k && i < seedList1.size(); i++) {
            set1.add(seedList1.get(i));
        }

        for (int i = 0; i < k && i < seedList2.size(); i++) {
            if (set1.contains(seedList2.get(i))) {
                counter.incrementCount(getNormalizedPosTag(seedList2.get(i)), 1.0);
            }
        }

        return counter;
    }

    private String getNormalizedPosTag(String word) {
        String tag = tagger.tagSentence(Sentence.toWordList(word)).get(0).tag();
        return tag.substring(0, Math.min(2, tag.length()));
    }
}
