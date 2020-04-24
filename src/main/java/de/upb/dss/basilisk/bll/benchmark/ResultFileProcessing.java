package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class processes the benchmarked results file.
 */
public class ResultFileProcessing {
    /**
     * This method renames all the result file name and move to the results folder.
     *
     * @param repoName Repository name.
     * @param tag      Repository tag.
     * @throws IOException If fails to read the Iguana suite.id
     */
    public static void renameResults(String repoName, String tag) throws IOException {
        ApplicationPropertiesUtils myAppUtils = new ApplicationPropertiesUtils();

        File iguanaPath = new File(myAppUtils.getIguanaPath());
        BufferedReader Buff = new BufferedReader(new FileReader(
                myAppUtils.getIguanaIdPath()
        ));
        String id = Buff.readLine();

        String result1 = myAppUtils.getResultPrefix() + id + "-1-1.nt";
        String result2 = myAppUtils.getResultPrefix() + id + "-1-2.nt";
        String result3 = myAppUtils.getResultPrefix() + id + "-1-3.nt";
        String result4 = myAppUtils.getResultPrefix() + id + "-1-4.nt";
        String result5 = myAppUtils.getResultPrefix() + id + "-1-5.nt";

        String cmd = "mv " + result1 + " ../results/" + repoName + "_" + tag + "_noClient1.nt";

        Runtime.getRuntime().exec(cmd, null, iguanaPath);

        cmd = "mv " + result2 + " ../results/" + repoName + "_" + tag + "_noClient4.nt";

        Runtime.getRuntime().exec(cmd, null, iguanaPath);

        cmd = "mv " + result3 + " ../results/" + repoName + "_" + tag + "_noClient8.nt";

        Runtime.getRuntime().exec(cmd, null, iguanaPath);

        cmd = "mv " + result4 + " ../results/" + repoName + "_" + tag + "_noClient16.nt";

        Runtime.getRuntime().exec(cmd, null, iguanaPath);

        cmd = "mv " + result5 + " ../results/" + repoName + "_" + tag + "_noClient32.nt";

        Runtime.getRuntime().exec(cmd, null, iguanaPath);

        Buff.close();
    }
}
