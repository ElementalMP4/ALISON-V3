package main.java.de.voidtech.alison.service;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Service
@Order(1)
public class ConfigService {

    private final Properties config;

    public ConfigService() {
        this.config = new Properties();
        File configFile = new File("AlisonConfig.properties");

        if (!configFile.exists()) {
           throw new RuntimeException("There is no config file. You need a file called AlisonConfig.properties at the root of the project!");
        }

        try {
            final FileInputStream fis = new FileInputStream(configFile);
            config.load(fis);
            fis.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("an error has occurred while reading the config: " + e.getMessage());
        }
    }

    public String getToken() {
        return this.config.getProperty("discord_token");
    }

    public String getDefaultPrefix() {
        final String prefix = this.config.getProperty("discord_prefix");
        return (prefix != null) ? prefix : "a!";
    }

    public String getMaster() {
        final String master = this.config.getProperty("master");
        return (master != null) ? master : "497341083949465600";
    }

    public String getHibernateDialect() {
        final String dialect = this.config.getProperty("hibernate.Dialect");
        return (dialect != null) ? dialect : "org.hibernate.dialect.PostgreSQLDialect";
    }

    public String getDriver() {
        final String driver = this.config.getProperty("hibernate.Driver");
        return (driver != null) ? driver : "org.postgresql.Driver";
    }

    public String getDBUser() {
        final String user = this.config.getProperty("hibernate.User");
        return (user != null) ? user : "postgres";
    }

    public String getDBPassword() {
        final String pass = this.config.getProperty("hibernate.Password");
        return (pass != null) ? pass : "postgres";
    }

    public String getConnectionURL() {
        final String dbURL = this.config.getProperty("hibernate.ConnectionURL");
        return (dbURL != null) ? dbURL : "jdbc:postgresql://localhost:5432/Alison";
    }

}