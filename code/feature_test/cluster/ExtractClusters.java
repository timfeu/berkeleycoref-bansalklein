package feature_test.cluster;

import edu.berkeley.nlp.util.Logger;
import feature_test.HeadPairExtractor;
import fig.basic.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class ExtractClusters extends HeadPairExtractor {
    private final String basePath_clusters;

    public final HashMap<String, TreeSet<Integer>> clusterMemberships = new HashMap<>();

    public ExtractClusters(String filePath_corefPairs, String basePath_clusters) {
        super(filePath_corefPairs, "");
        this.basePath_clusters = basePath_clusters;
    }

    public void loadClusters() throws IOException {
        long totalPhraseCnt = 0;

        for (int i = 1; i <= 10; i++) {
            Logger.logss("Loading phrase cluster file " + i + "...");
            File file = new File(basePath_clusters + "/phraseClusters." + i + ".txt.gz");
            BufferedReader br = IOUtils.openIn(file);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\t");
                if (!clusterMemberships.containsKey(parts[0])) continue;

                assert clusterMemberships.get(parts[0]).isEmpty() : "twice the same word in cluster list: " + parts[0];
                TreeSet<Integer> clusterIds = new TreeSet<>();

                for (int k = 1; k < parts.length; k++) {
                    if (k % 2 == 1) {
                        clusterIds.add(Integer.valueOf(parts[k]));
                    }
                }

                clusterMemberships.put(parts[0], clusterIds);
            }

            br.close();
        }
    }

    public void extract() {
        try {
            loadHeadPairs();

            for (String head : set_words) {
                clusterMemberships.put(normalizeDigits(head), new TreeSet<Integer>());
            }

            loadClusters();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // the clusters were computed on words with all digits replaced with 0
    public static String normalizeDigits(String head) {
        return head.replaceAll("[0-9]", "0");
    }

    @Override
    protected void countNGram(int n, String ngram) {
        throw new UnsupportedOperationException("We are not using google ngrams");
    }
}
