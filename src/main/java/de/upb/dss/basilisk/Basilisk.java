package de.upb.dss.basilisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Properties;

@SpringBootApplication
public class Basilisk {
    public static Properties applicationProperties;

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
