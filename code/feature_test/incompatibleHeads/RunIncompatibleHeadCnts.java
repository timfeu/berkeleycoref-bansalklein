package feature_test.incompatibleHeads;

import edu.berkeley.nlp.util.Logger;
import fig.basic.Option;
import fig.basic.Pair;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Not a Bansal & Klein feature: Find head pairs in patterns of incompatibility
 * ("Identifying synonyms among distributionally similar words", Lin et al. 2003)
 */
public class RunIncompatibleHeadCnts implements Runnable {
    @Option(gloss = "input path for the candidate coreference headword pairs to get co-occurrence counts of")
    public String filePath_corefPairs = null;

    @Option(gloss = "input path for the directory that contains Web1T Google n-gram files (the folder that contains " +
            "the 1gms, 2gms, ... subdirectories)")
    public String basePath_googlengrams = null;

    @Option(gloss = "this feature hyperparameter tells us binning size of the normalized co-occurrence count")
    public double featureParam_binSize = 1.0;

    @Option(gloss = "output path for writing the headword-pair queries and the feature(s) fired on them")
    public String filePathWrite = null;

    @Override
    public void run() {
        Logger.logss("Extracting incompatibility counts");
        ExtractIncompatibleHeadCnts extractor = new ExtractIncompatibleHeadCnts(filePath_corefPairs,
                basePath_googlengrams);
        extractor.extract();


        // write features
        FeatureIncompatibleHeadCnts featureGenerator = new FeatureIncompatibleHeadCnts(featureParam_binSize, extractor);

        try (FileWriter fw = new FileWriter(filePathWrite)) {

            for (Pair<String, String> pair : extractor.getSet_pairs()) {
                fw.append(pair.getFirst()).append(" ").append(pair.getSecond()).append(" ").append(featureGenerator
                        .produceValue(pair.getFirst(), pair.getSecond())).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
