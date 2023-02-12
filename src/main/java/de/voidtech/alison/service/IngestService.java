package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.PersistentAlisonWord;
import main.java.de.voidtech.alison.entities.PersistentClairePair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IngestService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TextGenerationService textGenerationService;

    @Autowired
    private ClaireService claireService;

    public static final Logger LOGGER = Logger.getLogger(IngestService.class.getSimpleName());

    public void ingestClaire() {
        SQLiteInterface db = new SQLiteInterface();
        ResultSet rs = db.getAllMessagePairs();
        LOGGER.log(Level.INFO, "Ingesting CLAIRE data...");
        try {
            while(rs.next()) {
                try (Session session = sessionFactory.openSession()) {
                    session.getTransaction().begin();
                    session.saveOrUpdate(new PersistentClairePair(
                            unescapeString(rs.getString("message")),
                            unescapeString(rs.getString("reply"))));
                    session.getTransaction().commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.log(Level.INFO, "Finished ingesting CLAIRE data");
    }

    public void ingestAlison() {
        SQLiteInterface db = new SQLiteInterface();
        ResultSet rs = db.getAllAlisonData();
        LOGGER.log(Level.INFO, "Ingesting ALISON data...");
        try {
            while(rs.next()) {
                try (Session session = sessionFactory.openSession()) {
                    session.getTransaction().begin();
                    session.saveOrUpdate(new PersistentAlisonWord(rs.getString("collection"),
                            unescapeString(rs.getString("word")),
                            unescapeString(rs.getString("next"))));
                    session.getTransaction().commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.log(Level.INFO, "Finished ingesting ALISON data");
    }

    private String escapeString(String in) {
        return in.replaceAll("'", "/@/");
    }

    private String unescapeString(String in) {
        return in.replaceAll("/@/", "'");
    }

    public void exportClaire() {
        List<PersistentClairePair> clairePairs = claireService.getAllPairs();
        SQLiteInterface db = new SQLiteInterface();
        LOGGER.log(Level.INFO, "Exporting CLAIRE data...");
        for (PersistentClairePair pair : clairePairs) {
            db.executeUpdate(String.format("INSERT INTO MessagePairs VALUES ('%s', '%s')",
                    escapeString(pair.getMessage()),
                    escapeString(pair.getReply())));
        }
        LOGGER.log(Level.INFO, "Finished exporting CLAIRE data");
    }

    public void exportAlison() {
        List<PersistentAlisonWord> alisonWords = textGenerationService.getAllWordsNoPack();
        SQLiteInterface db = new SQLiteInterface();
        LOGGER.log(Level.INFO, "Exporting ALISON data...");
        for (PersistentAlisonWord pair : alisonWords) {
            db.executeUpdate(String.format("INSERT INTO AlisonData VALUES ('%s', '%s', '%s')",
                    escapeString(pair.getWord()),
                    escapeString(pair.getNext()),
                    pair.getCollection()));
        }
        LOGGER.log(Level.INFO, "Finished exporting ALISON data");
    }

    private class SQLiteInterface {
        private Connection connection;

        public ResultSet getAllMessagePairs() {
            return queryDatabase("SELECT * FROM MessagePairs");
        }

        public ResultSet getAllAlisonData() {
            return queryDatabase("SELECT * FROM AlisonData");
        }

        public SQLiteInterface() {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:Alison.db");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void executeUpdate(String query) {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "An SQL Exception has occurred: " + e.getMessage());
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