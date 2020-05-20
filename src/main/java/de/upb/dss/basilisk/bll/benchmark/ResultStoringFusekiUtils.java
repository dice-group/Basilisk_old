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
            graphName = "dockerTentris";
        } else if("tentris".equals(repoName)) {
            graphName = "gitTentris";
        }

        connection.load(graphName + ":" + tag + "$" + prefix, model);

        connection.commit();
        connection.close();
    }

    public static void processResultFIle(String tripleStoreName, String repoName, String tag) {
        File[] files = getFileList();

        String suffix = "";
        for (int i = 0; i < files.length; i++) {
            if(files[i].toString().endsWith("-1.nt")) {
                suffix = "1worker";
            } else if(files[i].toString().endsWith("-2.nt")) {
                suffix = "4workers";
            } else if(files[i].toString().endsWith("-3.nt")) {
                suffix = "8workers";
            } else if(files[i].toString().endsWith("-4.nt")) {
                suffix = "16workers";
            } else {
                suffix = "32workers";
            }

            loadNtFile(tripleStoreName, repoName, tag, files[i].getAbsolutePath(), suffix);
            try {
                Files.move(Paths.get(files[i].getAbsolutePath()),
                        Paths.get("./results/" + files[i].getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
