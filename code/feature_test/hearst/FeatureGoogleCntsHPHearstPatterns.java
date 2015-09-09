package feature_test.hearst;

/**
 * @author Mohit Bansal (EECS, UC Berkeley)
 * Coreference Semantics from Web Features (Bansal and Klein, ACL 2012)
 * <p/>
 * modified by Tim Feuerbach
 */

import edu.berkeley.nlp.util.Counter;

import java.util.HashMap;

/*
 * This feature type returns the log-binned normalized co-occurrence count (only based on occurrence of a Hearst-style pattern in between) of the 2 head-words.
 * See Section 3.2 of ACL2012 paper
 */

public class FeatureGoogleCntsHPHearstPatterns {
    final double binSize;
    private final HashMap<String, Counter<String>> patternsCntsMap;
    private final Counter<String> wordcntmap;

    public FeatureGoogleCntsHPHearstPatterns(double binSize, HashMap<String, Counter<String>> patternsCntsMap, Counter<String> wordcntmap) {
        this.binSize = binSize;
        this.patternsCntsMap = patternsCntsMap;
        this.wordcntmap = wordcntmap;
    }

    public String produceValue(String hn1, String hn2) {
        //M2 for empty heads, M1 for cnt = 0, and -17 to -6/binSize for log-binned [cnt(A,B)/cnt(A)*cnt(B)]
        String ans = "M2";
        if (!hn1.isEmpty() && !hn2.isEmpty()) {
            assert wordcntmap.containsKey(hn1) : hn1;
            assert wordcntmap.containsKey(hn2) : hn2;
            assert patternsCntsMap.containsKey(hn1) : hn1;

            double cnt1 = wordcntmap.getCount(hn1);
            double cnt2 = wordcntmap.getCount(hn2);

            //get the total-count of co-occurrence of the headword-pair based on Hearst-style patterns in middle
            double totalCnt_pair = patternsCntsMap.get(hn1).getCount(hn2);

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