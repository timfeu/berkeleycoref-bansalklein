package feature_test.entity;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import feature_test.HeadPairExtractor;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class ExtractGoogleEntitySeeds extends HeadPairExtractor {
    protected Map<String, Counter<String>> tmpSeeds = new HashMap<>();
    public Map<String, List<String>> seeds = new HashMap<>();
    public static final int MAX_SEEDS = 200;

    private HashSet<String> stopwords_lowercase;

    public ExtractGoogleEntitySeeds(String filePath_corefPairs, String basePath_googlengrams, String stopwordList) {
        super(filePath_corefPairs, basePath_googlengrams);
        loadStopWords(stopwordList);
        Logger.logss("Loaded stopwords from " + stopwordList);
    }

    private void loadStopWords(String stopwordList) {
        try {
            stopwords_lowercase = new HashSet<>();
            BufferedReader reader = new BufferedReader(new FileReader(stopwordList));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    stopwords_lowercase.add(line.trim().toLowerCase());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    Pattern hearstPatternNgram = Pattern.compile("^\\S+\\s(is|are|was|were)(\\s(a|an|the))?\\s\\S+\\s[0-9]+$");

    @Override
    protected void countNGram(int n, String ngram) {
        String[] tokens = ngram.split("[\\s\\t]");
        String h1 = tokens[0];
        String seed = tokens[n - 1];

        // if the first word is not in our headwords list, we can stop early
        if (!set_words.contains(h1)) return;

        if (!shouldSkip(seed) && hearstPatternNgram.matcher(ngram).matches()) {
            tmpSeeds.get(h1).incrementCount(seed, Double.parseDouble(tokens[n]));
        }
    }

    /**
     * Returns false if the given seed is either in the list of stop words or does not contain a single letter (we
     * want to ignore numbers, punctuation, etc.)
     */
    private boolean shouldSkip(String seed) {
        return stopwords_lowercase.contains(seed.toLowerCase()) || !seed.matches(".*[a-zA-Z].*");
    }

    public Map<String, List<String>> extract() {
        try {
            loadHeadPairs();

            for (String word : set_words) {
                tmpSeeds.put(word, new Counter<String>());
                seeds.put(word, Collections.<String>emptyList());
            }

            loadNgramsGetCnts(new int[]{3, 4});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String head : tmpSeeds.keySet()) {
            ArrayList<String> seedsList = new ArrayList<>();

            int i = 0;
            for (String seed : tmpSeeds.get(head).getSortedKeys()) {
                i++;
                if (i > MAX_SEEDS) break;
                seedsList.add(seed);
            }

            seeds.put(head, seedsList);
        }

        tmpSeeds = null;

        return seeds;
    }

}
