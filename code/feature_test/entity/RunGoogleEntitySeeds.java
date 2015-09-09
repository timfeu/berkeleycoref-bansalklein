package feature_test.entity;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import fig.basic.Option;
import fig.basic.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Creates both entity features from Section 3.3 of the B&K paper
 */
public class RunGoogleEntitySeeds implements Runnable {
    @Option(gloss = "input path for the candidate coreference headword pairs to get co-occurrence counts of", required = true)
    public String filePath_corefPairs = null;

    @Option(gloss = "input path for the directory that contains Web1T Google n-gram files (the folder that contains " +
            "the 1gms, 2gms, ... subdirectories)", required = true)
    public String basePath_googlengrams = null;

    @Option(gloss = "array of top seeds to compare, all mentioned will be printed out seperately. Maximum is 200", required = true)
    public int[] featureParam_topMatch = null;

    @Option(gloss = "array top seeds to check for dominant POS tag, all mentioned will be printed out seperately. " +
            "Maximum is 200", required = true)
    public int[] featureParam_topPosMatch = null;

    @Option(gloss = "output path for writing the headword-pair queries and the feature(s) fired on them", required = true)
    public String filePathWrite = null;

    @Option(gloss = "Path to the POS tagger model", required = true)
    public String modelFile = null;

    @Option(gloss = "Path to a list of stopwords that should not be considered a seed")
    public String stopwordsList = "stopwords.txt";

    @Override
    public void run() {
        Logger.logss("Performing entity analysis");
        ExtractGoogleEntitySeeds extractor = new ExtractGoogleEntitySeeds(filePath_corefPairs, basePath_googlengrams, stopwordsList);
        extractor.extract();

        Arrays.sort(featureParam_topMatch);
        Arrays.sort(featureParam_topPosMatch);

        // write the features
        FeatureGoogleEntitySeeds featureGenerator = new FeatureGoogleEntitySeeds(extractor, modelFile,
                featureParam_topMatch, featureParam_topPosMatch);

        Logger.logss("Writing feature values");
        try (FileWriter fw = new FileWriter(filePathWrite)) {
            // first line: k for simple seed matches
            StringBuilder sb = new StringBuilder();
            for (int k : featureParam_topMatch) {
                sb.append(k).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            fw.append(sb).append("\n");

            // second line: k for pos matches
            sb = new StringBuilder();
            for (int k : featureParam_topPosMatch) {
                sb.append(k).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            fw.append(sb).append("\n");

            // actual feature values
            for (Pair<String, String> headpair : extractor.getSet_pairs()) {
                fw.append(headpair.getFirst()).append(" ").append(headpair.getSecond()).append(" ");
                // first write simple seed values
                String[] featureValues = featureGenerator.getSimpleSeedsValues(headpair.getFirst(), headpair
                        .getSecond());
                for (int i = 0; i < featureValues.length; i++) {
                    fw.append(featureValues[i]);
                    if (i < featureValues.length - 1) fw.append(" ");
                }
                fw.append(" ");

                // second write pos values
                featureValues = featureGenerator.getPosSeedValues(headpair.getFirst(), headpair.getSecond());
                for (int i = 0; i < featureValues.length; i++) {
                    fw.append(featureValues[i]);
                    if (i < featureValues.length - 1) fw.append(" ");
                }
                fw.append("\n");
            }

            Logger.logss("done");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
