package feature_test.cluster;

import java.util.TreeSet;

public class FeatureClusters {
    private final ExtractClusters extractor;

    public FeatureClusters(ExtractClusters extractor) {
        this.extractor = extractor;
    }

    public String getFeatureValue(String h1, String h2) {
        String h1Normalized = ExtractClusters.normalizeDigits(h1);
        String h2Normalized = ExtractClusters.normalizeDigits(h2);
        assert(extractor.clusterMemberships.containsKey(h1Normalized));
        assert(extractor.clusterMemberships.containsKey(h2Normalized));

        TreeSet<Integer> clusterList1 = extractor.clusterMemberships.get(h1Normalized);
        TreeSet<Integer> clusterList2 = extractor.clusterMemberships.get(h2Normalized);

        if (clusterList1.isEmpty() || clusterList2.isEmpty()) return "M1";

        int match1 = getMatch(clusterList1, clusterList2);
        int match2 = getMatch(clusterList2, clusterList1);

        if (match1 == 0 && match2 == 0) return "NM";

        assert(match1 != 0 && match2 != 0) : "match must exist in both lists";

        return String.valueOf(match1 + match2); // no binning
    }

    private int getMatch(TreeSet<Integer> clusterList1, TreeSet<Integer> clusterList2) {
        int i = 0;
        for (int clusterId : clusterList1) {
            i++;
            if (clusterList2.contains(clusterId)) return i;
        }

        return 0;
    }
}
