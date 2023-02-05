package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.entities.AlisonWord;
import main.java.de.voidtech.alison.entities.PersistentAlisonWord;
import main.java.de.voidtech.alison.entities.PersistentClairePair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IngestService {

    @Autowired
    private SessionFactory sessionFactory;

    public static final Logger LOGGER = Logger.getLogger(IngestService.class.getSimpleName());

    public void ingestFiles(CommandContext context) {
        context.reply("Loading model files...");
        File[] modelFiles = new File("models/").listFiles();
        Map<String, List<AlisonWord>> dataMap = new HashMap<>();
        if (modelFiles == null) return;
        for (File modelFile : modelFiles) {
            dataMap.put(modelFile.getName(), load(modelFile.getName()));
            LOGGER.log(Level.INFO, "Loaded model " + modelFile.getName());
        }

        context.reply("Model files loaded. Ingesting...");

        for (String model : dataMap.keySet()) {
            List<AlisonWord> words = dataMap.get(model);
            for (AlisonWord word : words) {
                for (int i = 0; i < word.getFrequency(); i++) {
                    try (Session session = sessionFactory.openSession()) {
                        session.getTransaction().begin();
                        session.saveOrUpdate(new PersistentAlisonWord(model, word.getWord(), word.getNext()));
                        session.getTransaction().commit();
                    }
                }
            }
            LOGGER.log(Level.INFO, "Ingested model " + model);
        }
        context.reply("Finished ingesting ALISON models :D");
    }

    @SuppressWarnings("unchecked")
    private List<AlisonWord> load(String pack) {
        List<AlisonWord> words = null;
        try {
            FileInputStream fileInStream = new FileInputStream("models/" + pack + "/words.alison");
            ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
            words = (List<AlisonWord>) objectInStream.readObject();
            objectInStream.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    public void ingestClaireDB(CommandContext commandContext) {
        commandContext.reply("Loading CLAIRE data...");
        DatabaseInterface db = new DatabaseInterface();
        ResultSet rs = db.getAllMessagePairs();
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
        commandContext.reply("Finished loading CLAIRE data :D");
    }

    private class DatabaseInterface {
        private Connection connection;

        public ResultSet getAllMessagePairs() {
            return queryDatabase("SELECT * FROM MessagePairs");
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