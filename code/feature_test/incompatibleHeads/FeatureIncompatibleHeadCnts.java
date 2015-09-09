package feature_test.incompatibleHeads;

public class FeatureIncompatibleHeadCnts {
    final double binSize;
    private final ExtractIncompatibleHeadCnts extractor;

    public FeatureIncompatibleHeadCnts(double binSize, ExtractIncompatibleHeadCnts extractor) {
        this.binSize = binSize;
        this.extractor = extractor;
    }

    public String produceValue(String hn1, String hn2) {
        //M2 for empty heads, M1 for cnt = 0, and -17 to -6/binSize for log-binned [cnt(A,B)/cnt(A)*cnt(B)]
        String ans = "M2";
        if (!hn1.isEmpty() && !hn2.isEmpty()) {
            assert extractor.getWordCounts().containsKey(hn1) : hn1;
            assert extractor.getWordCounts().containsKey(hn2) : hn2;
            assert extractor.getHeadPairCounts().containsKey(hn1) : hn1;
            assert extractor.getHeadPairCounts().containsKey(hn2) : hn2;

            double cnt1 = extractor.getWordCounts().getCount(hn1);
            double cnt2 = extractor.getWordCounts().getCount(hn2);

            //get the total-count of both directions
            double totalCnt_pair = extractor.getHeadPairCounts().get(hn1).getCount(hn2) + extractor.getHeadPairCounts().get(hn2).getCount(hn1) ;

            if (cnt1 == 0 || cnt2 == 0 || totalCnt_pair == 0) ans = "M1";
            else {
                double cnt_ans1 = Math.log10(totalCnt_pair) - Math.log10(cnt1) - Math.log10(cnt2); //log[cnt(A,B)/cnt(A)*cnt(B)]
                int cnt_ans = Math.round((float) (cnt_ans1 / binSize)); //bin and round-off
                ans = cnt_ans + "";
            }
        }

        return ans;
    }
}
