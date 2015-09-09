package feature_test.hearst;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import feature_test.HeadPairExtractor;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ExtractGoogleCntsHPHearstPatterns extends HeadPairExtractor {

    // map containing query headword-pairs and their co-occuerence counts within hearst patterns
    public HashMap<String, Counter<String>> hearstCounts = new HashMap<String, Counter<String>>();

    // map containing query headwords and their unigram counts from Web1T
    public Counter<String> queryCounter_headwords = new Counter<>();

    public ExtractGoogleCntsHPHearstPatterns(String filePath_corefPairs, String basePath_googlengrams) {
        super(filePath_corefPairs, basePath_googlengrams);
    }

    Pattern hearstPatternNgram = Pattern.compile("^\\S+\\s((is|are|was|were|other\\sthan|such\\sas|,\\sincluding|," +
            "\\sespecially)(\\s(a|an|the))?|of(\\s(the|all)?)|(and|or)\\s(other|the\\sother|another))" +
            "\\s\\S+\\s[0-9]+$");

    @Override
    protected void countNGram(int n, String ngram) {
        String[] tokens = ngram.split("[\\s\\t]");
        String h1 = tokens[0];
        String h2 = tokens[n - 1];

        // if the first word is not in our headwords list, we can stop early
        if (!set_words.contains(h1)) return;

        assert tokens.length == (n + 1) : tokens.length; //n-gram and count

        double gCnt = Double.parseDouble(tokens[n]);

        // in case of 1-gms, we update the unigram headword query counts
        if (n == 1) {
            if (queryCounter_headwords.containsKey(h1)) queryCounter_headwords.incrementCount(h1, gCnt);
            return;
        }

        // else we use the 3-5 gms to update the hearst for the headword pair queries
        // the benefit of a trie is that we can move on if this ngram's first word is not in the first level of our trie
        // after that, if the last word of the ngram is not in the second level of our trie, we again move on to the
        // next ngram
        // because we are only matching 1st and last word of ngrams (1,2,3,4,5-grams) to query to get cnts of a,ab,
        // a*b,a**b,a***b resp
        // N.B.: we assume symmetric head pairs, that is for each pair h1 h2 there is a pair h2 h1, so we can
        // lookup set_words directly to fail early

        if (!set_words.contains(h2)) {
            return;
        }

        if (hearstPatternNgram.matcher(ngram).matches()) {
            hearstCounts.get(h1).incrementCount(h2, gCnt);
        }
    }

    public void extract() throws IOException {
        Logger.setFig();

        loadHeadPairs();

        Logger.logss("Creating initial count map...");
        for (String word : set_words) {
            queryCounter_headwords.setCount(word, 0);
            hearstCounts.put(word, new Counter<String>());
        }
        Logger.logss("done");

        loadNgramsGetCnts(new int[]{1, 3, 4, 5}); // we don't nead 2grams
    }
}
