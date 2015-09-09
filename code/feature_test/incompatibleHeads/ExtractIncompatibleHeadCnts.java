package feature_test.incompatibleHeads;

import edu.berkeley.nlp.util.Counter;
import feature_test.HeadPairExtractor;
import fig.basic.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractIncompatibleHeadCnts extends HeadPairExtractor {
    // map containing query headwords and their unigram counts from Web1T
    public Counter<String> queryCounter_headwords = new Counter<>();

    // map containing query headword pairs and their pattern counts
    public HashMap<String, Counter<String>> queryCounter_headpairs = new HashMap<>();

    public ExtractIncompatibleHeadCnts(String filePath_corefPairs, String basePath_googlengrams) {
        super(filePath_corefPairs, basePath_googlengrams);
    }

    /**
     * Used patterns:
     * from X to Y
     * either X or Y
     * X vs . Y
     * X , Y and (other|similar)
     * between X and Y
     * battle of X and Y
     * X , unlike Y
     */
    Pattern hearstPatternNgram = Pattern.compile("^(?:from\\s(\\S+)\\sto\\s(\\S+)|either\\s(\\S+)\\sor\\s(\\S+)|" +
            "(\\S+)\\svs\\s\\.\\s(\\S+)|(\\S+)\\s,\\s(\\S+)\\sand\\s(?:similar|other)|between\\s(\\S+)\\sand\\s(\\S+)" +
            "|battle\\sof\\s(\\S+)\\sand\\s(\\S+)|(\\S+)\\s,\\sunlike\\s(\\S+))\\s[0-9]+$");

    @Override
    protected void countNGram(int n, String ngram) {
        Pair<String, String> reusablePair = new Pair<>("", "");

        if (n == 1) {
            String[] tokens = ngram.split("[\\s\\t]");
            String h1 = tokens[0];
            double gCnt = Double.parseDouble(tokens[n]);

            // unigram counts
            if (set_words.contains(h1)) queryCounter_headwords.incrementCount(h1, gCnt);
        } else {
            Matcher matcher = hearstPatternNgram.matcher(ngram);

            if (matcher.matches()) {
                assert (matcher.groupCount() == 2);
                String h1 = matcher.group(1);
                String h2 = matcher.group(2);

                String[] tokens = ngram.split("[\\s\\t]");
                double gCnt = Double.parseDouble(tokens[n]);

                reusablePair.setFirst(h1);
                reusablePair.setSecond(h2);

                if (set_pairs.contains(reusablePair)) {
                    queryCounter_headpairs.get(h1).incrementCount(h2, gCnt);
                }
            }
        }
    }

    public void extract() {
        try {
            loadHeadPairs();

            for (String headword : set_words) {
                queryCounter_headwords.setCount(headword, 0);
                queryCounter_headpairs.put(headword, new Counter<String>());

            }

            loadNgramsGetCnts(new int[]{1, 4, 5});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public HashMap<String, Counter<String>> getHeadPairCounts() {
        return queryCounter_headpairs;
    }

    public Counter<String> getWordCounts() {
        return queryCounter_headwords;
    }
}
