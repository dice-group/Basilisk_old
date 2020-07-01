package de.upb.dss.basilisk.bll.gitHook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This provides the methods to extract the zip file.
 *
 * @author Jalaj Bajpai
 */
public class Extraction {
    private static final int BUFFER_SIZE = 4096;

    /**
     * This method extracts the fuesiki zip file.
     *
     * @param zipFilePath   Path to the fuesiki zip file.
     * @param destDirectory Destination.
     * @throws IOException If fails to unzip the file.
     */
    public boolean unzipJena(String zipFilePath, String destDirectory) throws IOException {
        boolean flag = false;
        ZipFile zipFile = new ZipFile(zipFilePath);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        InputStream stream = null;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.toString().contains("/jena-fuseki2/")) {
                if (!entry.isDirectory()) {
                    stream = zipFile.getInputStream(entry);
                    extractFiles(stream, destDirectory
                            + entry.getName().substring(entry.getName().lastIndexOf("/jena-fuseki2/") + 13));
                } else {
                    File dir = new File(destDirectory
                            + entry.getName().substring(entry.getName().lastIndexOf("/jena-fuseki2/") + 13));
                    dir.mkdirs();
                }
                flag = true;
            } else {
                continue;
            }
        }

        if (stream != null)
            stream.close();

        if (zipFile != null)
            zipFile.close();

        return flag;
    }

    /**
     * This method extracts the generic zip file.
     *
     * @param zipFilePath    Path to the generic zip file.
     * @param destDirectory1 Destination.
     * @throws IOException If fails to unzip the file.
     */
    public void unzipGeneric(String zipFilePath, String destDirectory1) throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        InputStream stream = null;

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                stream = zipFile.getInputStream(entry);
                extractFiles(stream, destDirectory1 + "/" + entry.getName().substring(entry.getName().indexOf("/")));
            } else {
                File dir = new File(destDirectory1 + "/" + entry.getName().substring(entry.getName().indexOf("/")));
                dir.mkdirs();
            }

        }

        if (stream != null)
            stream.close();

        if (zipFile != null)
            zipFile.close();
    }

    /**
     * This method extracts the zip file.
     *
     * @param zipIn    Input stream to a zip file.
     * @param filePath File path to which the zip file should be extracted.
     * @throws IOException If fails to extract the zip file.
     */
    private void extractFiles(InputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}