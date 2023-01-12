package main.java.de.voidtech.alison.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MongoDBService {

    @Autowired
    private ConfigService config;

    private MongoClient mongoClient;

    private Map<String, MongoDatabase> dbCache = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void connect() {
        try {
            mongoClient = MongoClients.create(config.getMongoConnectionURL());
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    private MongoDatabase getDatabase(String database) {
        if (!dbCache.containsKey(database)) {
            dbCache.put(database, mongoClient.getDatabase(database));
        }
        return dbCache.get(database);
    }

    public Document query(String database, Bson command) {
        MongoDatabase db = getDatabase(database);
        return db.runCommand(command);
    }

    public boolean ping() {
        Document result = query("admin", new BsonDocument("ping", new BsonInt64(1)));
        return result.getDouble("ok") == 1.0;
    }
}
