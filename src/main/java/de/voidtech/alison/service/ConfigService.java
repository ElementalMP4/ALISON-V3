package main.java.de.voidtech.alison.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class ConfigService
{
    private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());;
    private final Properties config;
    
    public ConfigService() {
        this.config = new Properties();
        final File configFile = new File("AlisonConfig.properties");
        if (configFile.exists()) {
            try {
                final FileInputStream fis = new FileInputStream(configFile);
                try {
                    this.config.load(fis);
                    fis.close();
                }
                catch (Throwable t) {
                    try {
                        fis.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                    throw t;
                }
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, "an error has occurred while reading the config\n" + e.getMessage());
            }
        }
        else {
            LOGGER.log(Level.SEVERE, "There is no config file. You need a file called AlisonConfig.properties at the root of the project!");
        }
    }
    
    public String getToken() {
        return this.config.getProperty("discord_token");
    }
    
    public String getDefaultPrefix() {
        final String prefix = this.config.getProperty("default_prefix");
        return (prefix != null) ? prefix : "a!";
    }
    
    public String getMaster() {
        final String master = this.config.getProperty("master");
        return (master != null) ? master : "497341083949465600";
    }
    
    public String getMongoConnectionURL() {
        final String dbURL = this.config.getProperty("mongodb_url");
        return (dbURL != null) ? dbURL : "mongodb://localhost:27017/?maxPoolSize=20&w=majority";
    }
}
