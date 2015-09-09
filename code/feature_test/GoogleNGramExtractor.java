package feature_test;

import edu.berkeley.nlp.util.Logger;
import fig.basic.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public abstract class GoogleNGramExtractor {
    String basePath_googlengrams;

    public GoogleNGramExtractor(String basePath_googlengrams) {
        this.basePath_googlengrams = basePath_googlengrams;
    }

    protected void loadNgramsGetCnts() throws IOException {
        loadNgramsGetCnts(new int[] {1, 2, 3, 4, 5});
    }

    protected void loadNgramsGetCnts(int[] ngramsToLoad) throws IOException {
        long totalNgramCnt = 0;
        // go through each n-gram directory: use 1gms for headword counts, and 2-5gms for co-occurrence counts with varying number of wildcards/gaps
        for(int n : ngramsToLoad){
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
                    countNGram(n, ngram);
                    ngram = br.readLine();
                }
                br.close();
            }
            Logger.logss("read " + ngramCnt + " " + n + "-grams");
        }
        Logger.logss("read total of " + totalNgramCnt + " n-grams");
    }

    /**
     * gets one line of a Google ngram to count
     * @param n
     * @param ngram
     */
    protected abstract void countNGram(int n, String ngram);
}
