package feature_test.generalCoOccurr;

/**
 * @author Mohit Bansal (EECS, UC Berkeley)
 * Coreference Semantics from Web Features (Bansal and Klein, ACL 2012) 
 */

import java.util.HashMap;
import java.util.Map;

/*
 * This feature type returns the log-binned normalized co-occurrence count (sum of various wildcard-based counts) of the 2 head-words.
 * See Section 3.1 of ACL2012 paper
 */

public class FeatureGoogleCntsHPContGap {

	// this hyperparameter tells us binning size (see math below)
	double binSize;
	// this hyperparameter tells us the type of sum we use for the various wildcard-based co-occurrence counts
	// for e.g., "0123" means cnt('h1 h2') + cnt('h1 * h2') + cnt('h1 * * h2') + cnt('h1 * * * h2')
	String sumType;
	// wordcntmap contains the Web-count for each unigram headword
	HashMap<String, Double> wordcntmap = null;
	// headpairmap contains the Web-counts (4 types of counts for 0,1,2,3 wildcards, respectively) for each headword-pair
	HashMap<String, double[]> headpairmap = null;
	
	public FeatureGoogleCntsHPContGap(double binSize, String sumType, HashMap<String, double[]> headpairmap, HashMap<String, Double> wordcntmap) {
		this.binSize = binSize;
		this.sumType = sumType;
		this.headpairmap = headpairmap;
		this.wordcntmap = wordcntmap;
	}

	public String produceValue(String hn1, String hn2)
	{
		String headpair = hn1 + " " + hn2;

		//M2 for empty heads, M1 for cnt = 0, and -20 to 0/binSize for log-binned [cnt(A,B)/cnt(A)*cnt(B)]
		String ans = "M2";
		if (!hn1.isEmpty() && !hn2.isEmpty()) {
			assert wordcntmap.containsKey(hn1) : hn1;
			assert wordcntmap.containsKey(hn2) : hn2;
			assert headpairmap.containsKey(headpair) : headpair;

			double cnt1 = wordcntmap.get(hn1);
			double cnt2 = wordcntmap.get(hn2);

			double[] cnts_pair = headpairmap.get(headpair);
			double cnt_pair;
			// set the cnt(h1,h2) based on sumType hyperparameter
			if(sumType.equals("0123")) cnt_pair = cnts_pair[0] + cnts_pair[1] + cnts_pair[2] + cnts_pair[3];
			else if(sumType.equals("123")) cnt_pair = cnts_pair[1] + cnts_pair[2] + cnts_pair[3];
			else if(sumType.equals("23")) cnt_pair = cnts_pair[2] + cnts_pair[3];
			else { assert sumType.equals("3") : sumType; cnt_pair = cnts_pair[3]; }

			if (cnt1 == 0 || cnt2 == 0 || cnt_pair == 0) ans = "M1";
			else {
				double cnt_ans1 = Math.log10(cnt_pair) - Math.log10(cnt1) - Math.log10(cnt2); //log[cnt(A,B)/cnt(A)*cnt(B)]
				int cnt_ans = Math.round((float)(cnt_ans1/binSize)); //bin and round-off
				
				assert cnt_ans >= Math.round((float)(-20/binSize)) && cnt_ans <= 0 : cnt_ans;
				ans = cnt_ans + "";
			}
		}

		return ans;
	}

}
