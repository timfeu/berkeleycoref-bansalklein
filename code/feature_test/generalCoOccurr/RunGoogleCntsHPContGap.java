package feature_test.generalCoOccurr;

/**
 * @author Mohit Bansal (EECS, UC Berkeley)
 * Coreference Semantics from Web Features (Bansal and Klein, ACL 2012)
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.berkeley.nlp.util.Logger;
import fig.basic.Option;
import fig.exec.Execution;

/*
 * This class is a wrapper that first extracts the co-occurrence counts for 
 * headowrd pairs from Web1T and then computes features using these counts
 * See Section 3.1 of ACL2012 paper
 */

public class RunGoogleCntsHPContGap implements Runnable {

    @Option(gloss = "input path for the candidate coreference headword pairs to get co-occurrence counts of")
    public String filePath_corefPairs = null;

    @Option(gloss = "input path for the directory that contains Web1T Google n-gram files (the folder that contains the 1gms, 2gms, ... subdirectories)")
    public String basePath_googlengrams = null;

    @Option(gloss = "this feature hyperparameter tells us binning size of the normalized co-occurrence count")
    public double featureParam_binSize = 1.0;

    @Option(gloss = "this hyperparameter tells us the type of sum we use for the various wildcard-based co-occurrence counts")
    // for e.g., "0123" means cnt('h1 h2') + cnt('h1 * h2') + cnt('h1 * * h2') + cnt('h1 * * * h2')
    public String featureParam_sumType = "123";

    @Option(gloss = "output path for writing the headword-pair queries and the feature(s) fired on them")
    public String filePathWrite = null;

    public static void main(String[] args) {
        Execution.run(args, new RunGoogleCntsHPContGap());
    }

    public void run() {
        Logger.logss("Extracting google counts");
        // extract the co-occurrence counts from the Web1T corpus for each input headword-pair
        ExtractGoogleCntsHPContGap extractor = new ExtractGoogleCntsHPContGap(filePath_corefPairs, basePath_googlengrams);
        extractor.extract();

        // use the extracted Web1T counts to generate features for each input headword-pair
        FeatureGoogleCntsHPContGap featureGenerator = new FeatureGoogleCntsHPContGap(featureParam_binSize, featureParam_sumType, extractor.counts_headwordpairs, extractor.counts_headwords);


        // finally, write the features per headowrd-pair into a file
        try

        {
            FileWriter fw = new FileWriter(filePathWrite);
            FileReader fr = new FileReader(filePath_corefPairs);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                String[] tokens = line.split(" ");
                assert tokens.length <= 2 : "Wrong format - should have only 2 (or less) words 'head_np1 head_np2' --> " + line;
                if (tokens.length < 2 || (tokens[0].isEmpty() || tokens[1].isEmpty())) {
                    line = br.readLine();
                    continue;
                } // broken format
                String featureValue = featureGenerator.produceValue(tokens[0], tokens[1]);
                fw.write(line + " " + featureValue);
                fw.write("\n");
                line = br.readLine();
            }
            br.close();
            fr.close();
            fw.close();
        } catch (
                IOException e
                )

        {
            e.printStackTrace();
        }

    }
}