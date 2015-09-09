package feature_test.pronoun;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import feature_test.GoogleNGramExtractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class ExtractPronounReplacementCounts extends GoogleNGramExtractor {
    private final String filePath_corefPairs;

    public static final String PLACEHOLDER = "<NONE>";

    public HashSet<String> headlines = new HashSet<>();

    // counts contexts and replaced, etc.
    public Counter<String> contextCounts = new Counter<>();
    public Counter<String> unigramCounts = new Counter<>();


    public ExtractPronounReplacementCounts(String filePath_corefPairs, String basePath_googlengrams) {
        super(basePath_googlengrams);
        this.filePath_corefPairs = filePath_corefPairs;
    }

    public void loadHeadPronounPairs() throws IOException {
        // reading the head-word pairs from Reconcile and get the unique headword-pairs and headwords in a set
        FileInputStream fileInput = new FileInputStream(filePath_corefPairs);
        BufferedReader br = new BufferedReader(new InputStreamReader((filePath_corefPairs.endsWith(".gz")) ? new
                GZIPInputStream(fileInput) : fileInput));
        String line = br.readLine();
        int totalCnt = 0;
        while (line != null) {
            totalCnt++;
            String[] tokens = line.trim().split(" ");
            assert tokens.length == 5 : "Wrong format - should have two headwords followed by two context words and " +
                    "isPossessive value";

            headlines.add(line.trim());

            String pronoun = tokens[0];
            String replacement = tokens[1];
            String r1 = tokens[2];
            String r2 = tokens[3];
            boolean isPossessive = Boolean.valueOf(tokens[4]);

            unigramCounts.setCount(replacement, 0.0);

            // implicit signal to the extractor: if it has a key in the counts, it should be counted
            if (!PLACEHOLDER.equals(r1)) {
                if (isPossessive) {
                    // R1
                    contextCounts.setCount(String.format("<*> 's %s", r1), 0.0);
                    contextCounts.setCount(String.format("%s 's %s", replacement, r1), 0.0);
                } else {
                    // R1
                    contextCounts.setCount(String.format("<*> %s", r1), 0.0);
                    contextCounts.setCount(String.format("%s %s", replacement, r1), 0.0);
                }

                if (!PLACEHOLDER.equals(r2)) {

                    if (isPossessive) {
                        // R2
                        contextCounts.setCount(String.format("<*> 's %s %s", r1, r2), 0.0);
                        contextCounts.setCount(String.format("%s 's %s %s", replacement, r1, r2), 0.0);

                        // R1Gap
                        contextCounts.setCount(String.format("<*> 's <*> %s", r2), 0.0);
                        contextCounts.setCount(String.format("%s 's <*> %s", replacement, r2), 0.0);
                    } else {
                        // R2
                        contextCounts.setCount(String.format("<*> %s %s", r1, r2), 0.0);
                        contextCounts.setCount(String.format("%s %s %s", replacement, r1, r2), 0.0);

                        // R1Gap
                        contextCounts.setCount(String.format("<*> <*> %s", r2), 0.0);
                        contextCounts.setCount(String.format("%s <*> %s", replacement, r2), 0.0);
                    }
                }
            }

            line = br.readLine();
        }
        br.close();
        Logger.logss("Number of total 'head_np1 head_np2' instances = " + totalCnt);
    }


    @Override
    protected void countNGram(int n, String ngram) {
        String[] tokens = ngram.split("[\\s\\t]");

        assert tokens.length == (n + 1) : tokens.length; //n-gram and count

        double gCnt = Double.parseDouble(tokens[n]);

        if (n == 1) {
            // unigram counts: replacement words
            if (unigramCounts.containsKey(tokens[0])) {
                unigramCounts.incrementCount(tokens[0], gCnt);
            }
        } else if (n == 2) {
            // r1 pattern <*> r
            incrementPatternIfExists(String.format("<*> %s", tokens[1]), gCnt);
            // r1 pattern h1 r
            incrementPatternIfExists(String.format("%s %s", tokens[0], tokens[1]), gCnt);
        } else if (n == 3) {
            if (tokens[1].equals("'s")) {
                // r1 pattern <*> 's r
                incrementPatternIfExists(String.format("<*> 's %s", tokens[2]), gCnt);
                // r1 pattern h1 's r
                incrementPatternIfExists(String.format("%s 's %s", tokens[0], tokens[2]), gCnt);
            }
            // r2 pattern <*> r r'
            incrementPatternIfExists(String.format("<*> %s %s", tokens[1], tokens[2]), gCnt);
            // r2 pattern h1 r r'
            incrementPatternIfExists(String.format("%s %s %s", tokens[0], tokens[1], tokens[2]), gCnt);
            // r1gap pattern <*> <*> r'
            incrementPatternIfExists(String.format("<*> <*> %s", tokens[2]), gCnt);
            // r1gap pattern h1 <*> r'
            incrementPatternIfExists(String.format("%s <*> %s", tokens[0], tokens[2]), gCnt);
        } else if (n == 4) {
            if (tokens[1].equals("'s")) {
                // r2 pattern <*> 's r r'
                incrementPatternIfExists(String.format("<*> 's %s %s", tokens[2], tokens[3]), gCnt);
                // r2 pattern h1 's r r'
                incrementPatternIfExists(String.format("%s 's %s %s", tokens[0], tokens[2], tokens[3]), gCnt);
                // r1gap pattern <*> 's <*> r'
                incrementPatternIfExists(String.format("<*> 's <*> %s", tokens[3]), gCnt);
                // r1gap pattern h1 's <*> r'
                incrementPatternIfExists(String.format("%s 's <*> %s", tokens[0], tokens[3]), gCnt);
            }
        }

    }

    private void incrementPatternIfExists(String pattern, double gCnt) {
        if (contextCounts.containsKey(pattern)) {
            contextCounts.incrementCount(pattern, gCnt);
        }
    }

    public void extract() throws IOException {
        loadHeadPronounPairs();
        loadNgramsGetCnts(new int[]{1, 2, 3, 4});
    }
}
