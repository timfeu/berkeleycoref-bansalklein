package feature_test.cluster;

import edu.berkeley.nlp.util.Logger;
import fig.basic.Option;
import fig.basic.Pair;

import java.io.FileWriter;
import java.io.IOException;

public class RunClusters implements Runnable {
    @Option(gloss = "input path for the candidate coreference headword pairs to get co-occurrence counts of")
    public String filePath_corefPairs = null;

    @Option(gloss = "input path for the directory that contains the cluster .gz files (named 'phraseClusters.$i.txt.gz' for i in 1..10)")
    public String basePath_clusters = null;

    @Option(gloss = "output path for writing the headword-pair queries and the feature(s) fired on them")
    public String filePathWrite = null;

    @Override
    public void run() {
        Logger.logss("Running phrasal cluster matches");
        ExtractClusters extractor = new ExtractClusters(filePath_corefPairs, basePath_clusters);
        extractor.extract();
        FeatureClusters featureGenerator = new FeatureClusters(extractor);

        Logger.logss("Computing and writing headword cluster matches");
        try (FileWriter fw = new FileWriter(filePathWrite)) {

            for (Pair<String, String> pair : extractor.getSet_pairs()) {
                fw.append(pair.getFirst()).append(" ").append(pair.getSecond()).append(" ").append(featureGenerator.getFeatureValue(pair.getFirst(), pair.getSecond())).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
