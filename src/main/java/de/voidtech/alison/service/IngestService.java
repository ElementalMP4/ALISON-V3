package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.CommandContext;
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

    private void log(String message, CommandContext context) {
        LOGGER.log(Level.INFO, message);
        context.reply(message);
    }

    public void ingestClaire(CommandContext context) {
        SQLiteInterface db = new SQLiteInterface();
        ResultSet rs = db.getAllMessagePairs();
        log("Ingesting CLAIRE data...", context);
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
        log("Finished ingesting CLAIRE data", context);
    }

    public void ingestAlison(CommandContext context) {
        SQLiteInterface db = new SQLiteInterface();
        ResultSet rs = db.getAllAlisonData();
        log("Ingesting ALISON data...", context);
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
        log("Finished ingesting ALISON data", context);
    }

    private String escapeString(String in) {
        return in.replaceAll("'", "/@/");
    }

    private String unescapeString(String in) {
        return in.replaceAll("/@/", "'");
    }

    public void exportClaire(CommandContext context) {
        List<PersistentClairePair> clairePairs = claireService.getAllPairs();
        SQLiteInterface db = new SQLiteInterface();
        log("Exporting CLAIRE data...", context);
        for (PersistentClairePair pair : clairePairs) {
            try {
                db.executeUpdate(String.format("INSERT INTO MessagePairs VALUES ('%s', '%s')",
                    escapeString(pair.getMessage()),
                    escapeString(pair.getReply())));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        log("Finished exporting CLAIRE data", context);
    }

    public void exportAlison(CommandContext context) {
        List<PersistentAlisonWord> alisonWords = textGenerationService.getAllWordsNoPack();
        SQLiteInterface db = new SQLiteInterface();
        log("Exporting ALISON data...", context);
        for (PersistentAlisonWord pair : alisonWords) {
            try {
                db.executeUpdate(String.format("INSERT INTO AlisonData VALUES ('%s', '%s', '%s')",
                    escapeString(pair.getWord()),
                    escapeString(pair.getNext()),
                    pair.getCollection()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        log("Finished exporting ALISON data", context);
    }

    private class SQLiteInterface {
        private Connection connection;

        private static final String CREATE_CLAIRE_TABLE = "CREATE TABLE IF NOT EXISTS MessagePairs (message TEXT, reply TEXT)";
        private static final String CREATE_ALISON_TABLE = "CREATE TABLE IF NOT EXISTS AlisonData (word TEXT, next TEXT, collection TEXT)";


        public ResultSet getAllMessagePairs() {
            return queryDatabase("SELECT * FROM MessagePairs");
        }

        public ResultSet getAllAlisonData() {
            return queryDatabase("SELECT * FROM AlisonData");
        }

        public SQLiteInterface() {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:Alison.db");
                executeUpdate(CREATE_CLAIRE_TABLE);
                executeUpdate(CREATE_ALISON_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void executeUpdate(String query) throws SQLException {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
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