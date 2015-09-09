package feature_test;

import edu.berkeley.nlp.util.Logger;
import fig.basic.IOUtils;
import fig.basic.Pair;

import java.io.*;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public abstract class HeadPairExtractor extends GoogleNGramExtractor {
    protected String filePath_corefPairs = null;
    protected HashSet<Pair<String, String>> set_pairs = new HashSet<>();
    protected HashSet<String> set_words = new HashSet<>();

    public HeadPairExtractor(String filePath_corefPairs, String basePath_googlengrams) {
        super(basePath_googlengrams);
        this.filePath_corefPairs = filePath_corefPairs;
    }

    // load query headword-pairs into a hashset to remove duplicates, if any
    public void loadHeadPairs() throws IOException
    {
        // reading the head-word pairs from Reconcile and get the unique headword-pairs and headwords in a set
        FileInputStream fileInput = new FileInputStream(filePath_corefPairs);
        BufferedReader br = new BufferedReader(new InputStreamReader((filePath_corefPairs.endsWith(".gz")) ? new GZIPInputStream(fileInput) : fileInput));
        String line = br.readLine();
        int totalCnt = 0;
        int brokenCnt = 0;
        while(line != null){
            totalCnt++;
            String[] tokens = line.trim().split(" ");
            assert tokens.length >= 2 : "Wrong format - should have two headwords and optionally context words";
            // "house " gives tokens.length=1, " house" gives tokens.length=2 and " " gives tokens.length=0
            if(tokens[0].isEmpty() || tokens[1].isEmpty()) brokenCnt++;
            else set_pairs.add(new Pair<String, String>(tokens[0], tokens[1]));

            // adding the unigrams to a set
            if(tokens.length == 2){ //the "old house" case
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
        Logger.logss("Number of total 'head_np1 head_np2' instances = " + totalCnt);
        Logger.logss("Number of instances with empty head(s)= " + brokenCnt);
        Logger.logss("Number of unique 'head_np1 head_np2' instances = " + set_pairs.size());
        Logger.logss("Number of unique head words = " + set_words.size());
    }




    public HashSet<Pair<String, String>> getSet_pairs() {
        return set_pairs;
    }

    public HashSet<String> getSet_words() {
        return set_words;
    }
}
