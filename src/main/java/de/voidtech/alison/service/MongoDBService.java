package main.java.de.voidtech.alison.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MongoDBService {

    @Autowired
    private ConfigService config;

    private MongoClient mongoClient;

    @EventListener(ApplicationReadyEvent.class)
    private void connect() {
        try {
            mongoClient = MongoClients.create(config.getMongoConnectionURL());
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getCollection(String collection) {
        MongoDatabase db = mongoClient.getDatabase(config.getMongoDatabaseName());
        return db.getCollection(collection);
    }
}
