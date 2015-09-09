package feature_test.pronoun;

import fig.basic.Option;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Feature extractor for the pronoun context feature (section 3.5 on Bansal & Klein paper).
 */
public class RunPronounReplacementCounts implements Runnable {
    @Option(gloss = "input path for the candidate coreference headword pairs to get co-occurrence counts of")
    public String filePath_corefPairs = null;

    @Option(gloss = "input path for the directory that contains Web1T Google n-gram files (the folder that contains " +
            "the 1gms, 2gms, ... subdirectories)")
    public String basePath_googlengrams = null;

    @Option(gloss = "output path for writing the headword-pair queries and the feature(s) fired on them")
    public String filePathWrite = null;

    @Override
    public void run() {
        ExtractPronounReplacementCounts extractor = new ExtractPronounReplacementCounts(filePath_corefPairs,
                basePath_googlengrams);
        try {
            extractor.extract();
            FeaturePronounReplacementCounts featureGenerator = new FeaturePronounReplacementCounts(extractor);

            try (FileWriter fw = new FileWriter(filePathWrite)) {

                for (String line : extractor.headlines) {
                    fw.append(line).append(" ");
                    String[] features = featureGenerator.getFeature(line);
                    fw.append(features[0]).append(" ").append(features[1]).append(" ").append(features[2]).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
