package feature_test.generalCoOccurr;

/**
 * @author Mohit Bansal (EECS, UC Berkeley)
 * Coreference Semantics from Web Features (Bansal and Klein, ACL 2012) 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Logger;
import fig.basic.IOUtils;
import fig.basic.Pair;


/*
 * This class uses the Web1T Google-ngrams corpus to collect co-occurrence counts 
 * (with 0,1,2,3 wildcards in between) of various input headword-pairs to be used 
 * for the 'General Co-occurrence Feature'.
 * We use 1,2,3,4 and 5-grams to get counts of queries 'h1'|'h2', 'h1 h2', 
 * 'h1 * h2', 'h1 * * h2', 'h1 * * * h2', respectively.
 * IMPORTANT: we can use (k+2)-grams to get counts of 'h1 [k *] h2', 
 * i.e. we only match the border words of the (k+2)-gram to (h1,h2) 
 * because we assume that the Web1T dataset is complete such that 
 * marginalizing the (k+1)-grams will give us k-grams
 * the  
 * See Section 3.1 of ACL2012 paper
 */

public class ExtractGoogleCntsHPContGap
{

	// input path for the candidate coreference headword pairs to get co-occurrence counts of
	String filePath_corefPairs = null;
	//input path for the directory that contains Web1T Google n-gram files (the folder that contains the 1gms, 2gms, ... subdirectories)
	String basePath_googlengrams = null;
	// map containing query headword-pairs and their co-occuerence counts from Web1T (with 0,1,2,3 wildcards in between)
	HashMap<String, double[]> counts_headwordpairs = null;
	// map containing query headwords and their unigram counts from Web1T
	HashMap<String, Double> counts_headwords = null;
	
	
	public ExtractGoogleCntsHPContGap(String filePath_corefPairs, String basePath_googlengrams) {
		this.filePath_corefPairs = filePath_corefPairs;
		this.basePath_googlengrams = basePath_googlengrams;
	}
	
	
	public void extract()
	{
		Logger.setFig();

		// load query headword-pairs and headwords (for unigram counts to be used in normalization) into a hashset to remove duplicates, if any
		HashSet<String> set_pairs = new HashSet<String>();
		HashSet<String> set_words = new HashSet<String>();
		try {loadHeadPairs(set_pairs, set_words);
		} catch (IOException e) { e.printStackTrace(); }

		// create a query hashmap-based suffix-trie that has the first headword as the 1st level of the trie, 
		// the second headword as the 2nd level and the thrid level values are the co-occurrence counts 
		// array, with one count each for queries 'h1 h2', 'h1 * h2', 'h1 * * h2', 'h1 * * * h2', respectively.
		HashMap<String, HashMap<String,double[]>> queryMapTrie_headwordpairs = new HashMap<String, HashMap<String,double[]>>();
		createQueryMapTrie(set_pairs, queryMapTrie_headwordpairs);
		
		// also create a counter for the unigram-based headword queries
		Counter<String> queryCounter_headwords = new Counter<String>();
		for (String word : set_words) {
			queryCounter_headwords.setCount(word, 0);
		}
		
		try {
			// reading the Web1T Google ngram files and collecting the counts
			loadNgramsGetCnts(queryMapTrie_headwordpairs, queryCounter_headwords);
		} catch (IOException e) {e.printStackTrace();}
		
		// convert the trie and counters to hashmaps for use by the feature-generator
		convertToMap(set_pairs, queryMapTrie_headwordpairs, queryCounter_headwords);
	}
	
	// load query headword-pairs into a hashset to remove duplicates, if any
	public void loadHeadPairs(HashSet<String> set_pairs, HashSet<String> set_words) throws IOException
	{
		// reading the head-word pairs from Reconcile and get the unique headword-pairs and headwords in a set
		FileReader fr = new FileReader(filePath_corefPairs);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		int totalCnt = 0;
		int brokenCnt = 0;
		while(line != null){
			totalCnt++;
			String[] tokens = line.split(" ");
			assert tokens.length <= 2 : "Wrong format - should have only 2 (or less) words 'head_np1 head_np2' --> " + line;
			// "house " gives tokens.length=1, " house" gives tokens.length=2 and " " gives tokens.length=0
			if(tokens.length < 2 || (tokens[0].isEmpty() || tokens[1].isEmpty())) brokenCnt++;
			else set_pairs.add(line);
			
			// adding the unigrams to a set
			if(tokens.length == 2){ //the "old house" and " house" cases
				if(!tokens[0].isEmpty()) set_words.add(tokens[0]);
				if(!tokens[1].isEmpty()) set_words.add(tokens[1]);
			}
			else if(tokens.length == 1) { // the "house " case
				assert !tokens[0].isEmpty();
				set_words.add(tokens[0]);
			}
			else assert tokens.length == 0; // the " " case

			line  = br.readLine();
		}
		br.close();
		fr.close();
		Logger.logss("Number of total 'head_np1 head_np2' instances = " + totalCnt);
		Logger.logss("Number of instances with empty head(s)= " + brokenCnt);
		Logger.logss("Number of unique 'head_np1 head_np2' instances = " + set_pairs.size());
		Logger.logss("Number of unique head words = " + set_words.size());
	}

	
	// create a query hashmap-based suffix-trie that has the first headword as the 1st level of the trie, 
	// the second headword as the 2nd level and the thrid level values are the co-occurrence counts 
	// array, with one count each for queries 'h1 h2', 'h1 * h2', 'h1 * * h2', 'h1 * * * h2', respectively.
	public void createQueryMapTrie(HashSet<String> set_pairs, HashMap<String, HashMap<String,double[]>> queryMapTrie)
	{
		for(String phrase : set_pairs){
			String[] phrToks = phrase.split(" ");
			assert phrToks.length == 2 : Arrays.toString(phrToks);
			String firstWord = phrToks[0];
			String secondWord = phrToks[1];
			if(!queryMapTrie.containsKey(firstWord)){
				HashMap<String,double[]> value = new HashMap<String,double[]>();
				value.put(secondWord, new double[]{0,0,0,0});
				queryMapTrie.put(firstWord, value);
			}
			else{
				HashMap<String,double[]> value = queryMapTrie.get(firstWord);
				// this means that the full query q1 q2 has not been seen before in the list of queries
				// because we read the queries as a hashset and have hence already removed the duplicate ones
				assert !value.containsKey(secondWord);
				value.put(secondWord, new double[]{0,0,0,0});
				queryMapTrie.put(firstWord, value);
			}
		}
	}
	
	
	// reading the Web1T Google ngram files and collecting the counts
	public void loadNgramsGetCnts(HashMap<String, HashMap<String, double[]>> queryMapTrie_headwordpairs, 
			Counter<String> queryCounter_headwords) throws IOException
	{
		long totalNgramCnt = 0;
		// go through each n-gram directory: use 1gms for headword counts, and 2-5gms for co-occurrence counts with varying number of wildcards/gaps
		for(int n=1; n<=5; n++){
			Logger.logss("Loading google " + n + "-grams ... ");
			File dir = new File(basePath_googlengrams + "/" + n + "gms");
			String[] filesStr = dir.list();
			long ngramCnt = 0;
			for (String fileStr : filesStr){
				Logger.logss("Loading " + n + "-grams from file " + fileStr);
				BufferedReader br = IOUtils.openIn(basePath_googlengrams + "/" + n + "gms/" + fileStr);
				String ngram = br.readLine();  // WE DO NOT LOWER-CASE
				long fileCnt = 0;
				while(ngram != null){
					fileCnt++;
					ngramCnt++;
					totalNgramCnt++;
					if(fileCnt%1000000 == 0)Logger.logss("ngram # : " + fileCnt);
					getCntsHelper(n, ngram, queryMapTrie_headwordpairs, queryCounter_headwords);
					ngram = br.readLine();
				}
				br.close();
			}
			Logger.logss("read " + ngramCnt + " " + n + "-grams");
		}
		Logger.logss("read total of " + totalNgramCnt + " n-grams");
	}

	private void getCntsHelper(int n, String ngram, HashMap<String, HashMap<String, double[]>> queryMapTrie_headwordpairs, Counter<String> queryCounter_headwords)
	{
		String[] tokens = ngram.split("[\\s\\t]");
		assert tokens.length == (n + 1) : tokens.length; //n-gram and count
		String firstWordNgram = tokens[0];
		String lastWordNgram = tokens[n-1];
		double gCnt = Double.parseDouble(tokens[n]);
		// in case of 1-gms, we update the unigram headword query counts
		if (n == 1) {
			if(queryCounter_headwords.containsKey(firstWordNgram)) queryCounter_headwords.incrementCount(firstWordNgram, gCnt);
			return;
		}
		// else we use the 2-5 gms to update the coninguous and gapped counts for the headword pair queries
		// the benefit of a trie is that we can move on if this ngram's first word is not in the first level of our trie
		// after that, if the last word of the ngram is not in the second level of our trie, we again move on to the next ngram
		// because we are only matching 1st and last word of ngrams (1,2,3,4,5-grams) to query to get cnts of a,ab,a*b,a**b,a***b resp
		if(queryMapTrie_headwordpairs.containsKey(firstWordNgram)){
			HashMap<String,double[]> querySecondWordMap1 = queryMapTrie_headwordpairs.get(firstWordNgram);
			// for the last word of the n-gram, if it matches as 2nd-word of some query, then inc cnt of ab,a*b,a**b,a***b dep on whether it's a 2,3,4,5-gm resp
			if(querySecondWordMap1.containsKey(lastWordNgram)){
				double[] cnts = querySecondWordMap1.get(lastWordNgram);
				cnts[n-2] += gCnt; // increment the count of the corr bucket among ab,a*b,a**b,a***b dep on whether this is a 2,3,4, or 5 gram
				queryMapTrie_headwordpairs.get(firstWordNgram).put(lastWordNgram, cnts);
			}
		}
	}	
	
	public void convertToMap(HashSet<String> set_pairs, HashMap<String, HashMap<String, double[]>> queryMapTrie_headwordpairs, Counter<String> queryCounter_headwords) 
	{
		counts_headwordpairs = new HashMap<String, double[]>();
		for(String phrase : set_pairs){
			String[] phrToks = phrase.split(" ");
			assert queryMapTrie_headwordpairs.containsKey(phrToks[0]);
			assert queryMapTrie_headwordpairs.get(phrToks[0]).containsKey(phrToks[1]);
			double[] cnts = queryMapTrie_headwordpairs.get(phrToks[0]).get(phrToks[1]);
			assert !counts_headwordpairs.containsKey(phrase);
			counts_headwordpairs.put(phrase, cnts);
		}
		counts_headwords = new HashMap<String, Double>();
		for (String word : queryCounter_headwords.keySet()) {
			counts_headwords.put(word, queryCounter_headwords.getCount(word));
		}
	}


}