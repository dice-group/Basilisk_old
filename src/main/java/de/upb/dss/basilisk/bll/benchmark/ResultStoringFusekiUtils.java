package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is the utility class to support the process of storing the benchmark results into the Fuseki server.
 *
 * @author Ranjith Krishnamurthy
 */
public class ResultStoringFusekiUtils {
    private static final String logPrefix = "Result Processing";

    /**
     * This method returns the list of all result file available in the iguana directory.
     *
     * @return List of all result files available in the iguana directory.
     */
    private static File[] getFileList() {
        String iguanaPath = new ApplicationPropertiesUtils().getIguanaPath();

        File f = new File(iguanaPath);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("results_task.*.nt");
            }
        };

        return f.listFiles(filter);
    }

    /**
     * This method loads the results file into named graph in Fuseki server.
     *
     * @param tripleStoreName Currently running triple store name.
     * @param repoName        Currently running triple store's repository name.
     * @param tag             Tag of the currently running triple store.
     * @param ntFile          Name of the nt file to be loaded.
     * @param prefix          Prefix to the graph name in Fuseki server.
     */
    private static void loadNtFile(String tripleStoreName, String repoName, String tag, String ntFile, String suffix) {
        RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination(
                        new ApplicationPropertiesUtils().getResultStoringFusekiEndPoint()
                                + tripleStoreName);

        RDFConnection connection = builder.build();

        Model model = ModelFactory.createDefaultModel();
        model.read(ntFile);

        String graphName = "http://basilisk-cpb.de/";

        //Todo: Add new triple store information here to load into the Fuseki server.
        if ("dicegroup/tentris_server".equals(repoName)) {
            graphName += "docker/tentris";
        } else if ("tentris".equals(repoName)) {
            graphName += "git/tentris";
        } else if ("openlink/virtuoso-opensource-7".equals(repoName)) {
            graphName += "docker/virtuoso";
        } else if ("fuseki".equals(repoName)) {
            graphName += "git/fuseki";
        }

        LoggerUtils.logForBasilisk(logPrefix, "Loading " + ntFile + " into the Fuseki server.", 1);
        //Loads the file into the named graph.
        connection.put(graphName + "/" + tag + "/" + suffix, model);

        connection.commit();
        connection.close();
    }

    /**
     * This method loads the result file available in the iguana folder into Fuseki server and move the result files
     * into results directory.
     *
     * @param tripleStoreName Currently running triple store name.
     * @param repoName        Currently running triple store's repository name.
     * @param tag             Tag of the currently running triple store.
     */
    public static void processResultFIle(String tripleStoreName, String repoName, String tag) {
        File[] files = getFileList();

        String suffix;
        for (File file : files) {
            if (file.toString().endsWith("-1.nt")) {
                suffix = "1worker";
            } else if (file.toString().endsWith("-2.nt")) {
                suffix = "4workers";
            } else if (file.toString().endsWith("-3.nt")) {
                suffix = "8workers";
            } else if (file.toString().endsWith("-4.nt")) {
                suffix = "16workers";
            } else {
                suffix = "32workers";
            }

            //Loads the result file into the Fuseki server.
            loadNtFile(tripleStoreName, repoName, tag, file.getAbsolutePath(), suffix);
            try {
                Files.move(Paths.get(file.getAbsolutePath()),
                        Paths.get("./results/" + file.getName()));
            } catch (IOException e) {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while moving the result files into results directory.", 4);
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all the results file in the iguana directory.
     */
    public static void deleteUnknownResultFile() {
        LoggerUtils.logForBasilisk(
                logPrefix,
                "Checking for unknown result file Iguana directory, If found any, will be deleted.",
                4);

        for (File file : getFileList()) {
            try {
                Files.delete(Paths.get(file.getAbsolutePath()));
            } catch (IOException e) {
                LoggerUtils.logForBasilisk(logPrefix, "Something went wrong while deleting the " +
                        "unknown result file in Iguana folder", 4);
                e.printStackTrace();
            }
        }
    }
}
