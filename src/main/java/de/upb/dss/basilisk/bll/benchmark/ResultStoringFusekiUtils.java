package de.upb.dss.basilisk.bll.benchmark;

import de.upb.dss.basilisk.bll.applicationProperties.ApplicationPropertiesUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ResultStoringFusekiUtils {

    private static File[] getFileList() {
        String iguanaPath = new ApplicationPropertiesUtils().getIguanaPath();

        File f = new File(iguanaPath);

        FilenameFilter filter = (f1, name) -> {
            // We want to find only .c files
            return name.matches("results_task.*.nt");
        };

        return f.listFiles(filter);
    }

    private static void loadNtFile(String tripleStoreName, String repoName, String tag, String ntFile, String prefix) {
        RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
                .destination("http://131.234.28.165:3030/" + tripleStoreName);

        RDFConnection connection = builder.build();

        Model model = ModelFactory.createDefaultModel() ;
        model.read(ntFile) ;

        String graphName = "";

        if("dicegroup/tentris_server".equals(repoName)) {
            graphName = "dockertentris";
        } else if("tentris".equals(repoName)) {
            graphName = "gittentris";
        } else if("openlink/virtuoso-opensource-7".equals(repoName)) {
            graphName = "dockervirtuoso";
        }

        connection.load(graphName + ":" + tag + "$" + prefix, model);

        connection.commit();
        connection.close();
    }

    public static void processResultFIle(String tripleStoreName, String repoName, String tag) {
        File[] files = getFileList();

        String suffix = "";
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

            loadNtFile(tripleStoreName, repoName, tag, file.getAbsolutePath(), suffix);
            try {
                Files.move(Paths.get(file.getAbsolutePath()),
                        Paths.get("./results/" + file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
