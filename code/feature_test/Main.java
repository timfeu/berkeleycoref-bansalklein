package feature_test;

import feature_test.cluster.RunClusters;
import feature_test.entity.RunGoogleEntitySeeds;
import feature_test.generalCoOccurr.RunGoogleCntsHPContGap;
import feature_test.hearst.RunGoogleCntsHPHearstPatterns;
import feature_test.incompatibleHeads.RunIncompatibleHeadCnts;
import feature_test.pronoun.RunPronounReplacementCounts;
import fig.exec.Execution;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: runnable_wrapper_and_data.jar mode [options]");
            System.out.println("Where mode is one of 'generalCoOccurrence', 'hearst', 'entity', 'incompatibility', 'cluster', 'pronoun'");
            System.exit(0);
        }

        switch (args[0]) {
            case "generalCoOccurrence":
                Execution.run(getRemainingArgs(args), new RunGoogleCntsHPContGap());
                break;

            case "hearst":
                Execution.run(getRemainingArgs(args), new RunGoogleCntsHPHearstPatterns());
                break;

            case "entity":
                Execution.run(getRemainingArgs(args), new RunGoogleEntitySeeds());

            case "pronoun":
                Execution.run(getRemainingArgs(args), new RunPronounReplacementCounts());

            case "cluster":
                Execution.run(getRemainingArgs(args), new RunClusters());

                // not B&K
            case "incompatibility":
                Execution.run(getRemainingArgs(args), new RunIncompatibleHeadCnts());

            default:
                System.out.println("Mode must be one of 'generalCoOccurrence', 'hearst', 'entity', 'incompatibility', 'cluster', 'pronoun'");

        }
    }

    private static String[] getRemainingArgs(String[] args) {
        return (args.length > 1) ?
                Arrays.copyOfRange(args, 1, args.length) :
                new String[0];
    }
}
