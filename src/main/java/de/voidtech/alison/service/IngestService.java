package main.java.de.voidtech.alison.service;

import com.mongodb.Mongo;
import com.mongodb.client.MongoCollection;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.entities.AlisonWord;
import org.bson.Document;
import org.bson.types.ObjectId;
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
    private MongoDBService mongoDBService;

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
        MongoCollection<Document> collection = mongoDBService.getCollection("word_pairs");

        for (String model : dataMap.keySet()) {
            List<AlisonWord> words = dataMap.get(model);
            for (AlisonWord word : words) {
                for (int i = 0; i < word.getFrequency(); i++) {
                    collection.insertOne(new Document()
                            .append("_id", new ObjectId())
                            .append("word", word.getWord())
                            .append("next", word.getNext())
                            .append("collection", model));
                }
            }
            LOGGER.log(Level.INFO, "Ingested model " + model);
            context.reply("Ingested model " + model);
        }
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

    public void ingestClaireDB() {
        DatabaseInterface db = new DatabaseInterface();
        ResultSet rs = db.getAllMessagePairs();
        MongoCollection<Document> claireCollection = mongoDBService.getCollection("claire_heap");

        try {
            while(rs.next()) {
                claireCollection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("message", rs.getString("message").replaceAll("@", "``@``"))
                        .append("reply", rs.getString("reply").replaceAll("@", "``@``")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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