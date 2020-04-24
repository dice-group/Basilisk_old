package de.upb.dss.basilisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Properties;

/**
 * The main class of the Basilisk Spring application.
 */
@SpringBootApplication
public class Basilisk {
    public static Properties applicationProperties;

    /**
     * The main method of the Basilisk Spring application.
     * @param args Arguments to the Basilisk application.
     */
    public static void main(String[] args) {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "application.properties";

        applicationProperties = new Properties();
        try {
            applicationProperties.load(new FileInputStream(appConfigPath));
        } catch (Exception ex) {
            System.out.println("Error loading config : " + ex.getMessage());
        }

        SpringApplication.run(Basilisk.class, args);
    }
}
