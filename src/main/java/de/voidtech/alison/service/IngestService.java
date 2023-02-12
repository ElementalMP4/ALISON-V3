package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.PersistentAlisonWord;
import main.java.de.voidtech.alison.entities.PersistentClairePair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IngestService {

    @Autowired
    private SessionFactory sessionFactory;

    public static final Logger LOGGER = Logger.getLogger(IngestService.class.getSimpleName());

    public void ingestClaire() {
        LOGGER.log(Level.INFO, "Started ingesting SQLite data...");
        DatabaseInterface db = new DatabaseInterface();
        ResultSet rs = db.getAllMessagePairs();
        LOGGER.log(Level.INFO, "Ingesting CLAIRE data...");
        try {
            while(rs.next()) {
                try (Session session = sessionFactory.openSession()) {
                    session.getTransaction().begin();
                    session.saveOrUpdate(new PersistentClairePair(rs.getString("message"), rs.getString("reply")));
                    session.getTransaction().commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.log(Level.INFO, "Finished ingesting CLAIRE data");
    }

    public void ingestAlison() {
        DatabaseInterface db = new DatabaseInterface();
        ResultSet rs = db.getAllAlisonData();
        LOGGER.log(Level.INFO, "Ingesting ALISON data...");
        try {
            while(rs.next()) {
                try (Session session = sessionFactory.openSession()) {
                    session.getTransaction().begin();
                    session.saveOrUpdate(new PersistentAlisonWord(rs.getString("collection"), rs.getString("word"), rs.getString("next")));
                    session.getTransaction().commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.log(Level.INFO, "Finished ingesting ALISON data");
    }

    public void exportClaire() {

    }

    public void exportAlison() {

    }

    private class DatabaseInterface {
        private Connection connection;

        public ResultSet getAllMessagePairs() {
            return queryDatabase("SELECT * FROM MessagePairs");
        }

        public ResultSet getAllAlisonData() {
            return queryDatabase("SELECT * FROM AlisonData");
        }

        public DatabaseInterface() {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:Alison.db");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private ResultSet queryDatabase(String query) {
            try {
                Statement statement = connection.createStatement();
                return statement.executeQuery(query);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "An SQL Exception has occurred: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}