package main.java.de.voidtech.alison.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TextGenerationService {

    @Autowired
    private MongoDBService mongoDb;

    public static final int CLAIRE_LENGTH = 1000;
    public static final int NICKNAME_LENGTH = 32;
    public static final int QUOTE_LENGTH = 100;
    public static final int SEARCH_LENGTH = 50;
    public static final int IMITATE_LENGTH = 2000;
    public static final int PROMPT_LENGTH = 35;

    private String generateMessage(String wordCollectionName, int length) {
        StringBuilder result = new StringBuilder();

        Document databaseRecord = getRandomStartWord(wordCollectionName);

        if (databaseRecord == null) return null;

        String word = databaseRecord.getString("word");
        String nextWord = databaseRecord.getString("next");

        while (!nextWord.equals("StopWord")) {
            if (result.length() + (word + " ").length() > length) break;
            result.append(word).append(" ");
            List<Document> potentials = getWordList(wordCollectionName, nextWord);
            Document document = getRandomDocument(potentials);
            word = document.getString("word");
            nextWord = document.getString("next");
        }
        if (result.length() + word.length() <= length) result.append(word);
        return result.toString().replaceAll("<[^>]*>", "").replaceAll("@", "``@``");
    }

    public String createQuote(String wordCollection) {
        return generateMessage(wordCollection, QUOTE_LENGTH);
    }

    public String createSearch(String wordCollection) {
        return generateMessage(wordCollection, SEARCH_LENGTH);
    }

    public String createImitate(String wordCollection) {
        return generateMessage(wordCollection, IMITATE_LENGTH);
    }

    public String createNickname(String wordCollection) {
        return generateMessage(wordCollection, NICKNAME_LENGTH);
    }

    public String createConversationPrompt(String wordCollection) {
        return generateMessage(wordCollection, PROMPT_LENGTH);
    }

    private Document getRandomDocument(List<Document> potentials) {
        return potentials.get(new Random().nextInt(potentials.size()));
    }

    private List<Document> getWordList(String wordCollectionName, String nextWord) {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        List<Bson> aggregates = new ArrayList<>();
        aggregates.add(Aggregates.match(Filters.eq("word", nextWord)));
        if (!wordCollectionName.equals("SEARCH_ALL_COLLECTIONS"))
            aggregates.add(Aggregates.match(Filters.eq("collection", wordCollectionName)));

        MongoCursor<Document> cursor = collection.aggregate(aggregates).iterator();
        List<Document> result = new ArrayList<>();

        try {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    private Document getRandomStartWord(String wordCollectionName) {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        List<Bson> aggregates = new ArrayList<>();
        aggregates.add(Aggregates.sample(1));
        if (!wordCollectionName.equals("SEARCH_ALL_COLLECTIONS"))
            aggregates.add(Aggregates.match(Filters.eq("collection", wordCollectionName)));
        return collection.aggregate(aggregates).first();
    }

    public boolean dataIsAvailableForID(String id) {
        return getWordCountForCollection(id) > 0;
    }

    public long getWordCount() {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        return collection.countDocuments();
    }

    public long getModelCount() {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        long count = 0;
        MongoCursor<String> cursor = collection.distinct("collection", String.class).iterator();
        while (cursor.hasNext()) {
            count++;
        }
        return count;
    }

    public Map<String, Long> getTopFiveWords(String id) {
        //TODO: Implement this
        return new HashMap<>();
    }

    public long getWordCountForCollection(String id) {
        Bson query = Filters.eq("collection", id);
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        return collection.countDocuments(query);
    }

    public void delete(String id) {
        Bson query = Filters.eq("collection", id);
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        collection.deleteMany(query);
    }

    public List<String> getAllWords(String pack) {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        List<Bson> aggregates = new ArrayList<>();
        aggregates.add(Aggregates.match(Filters.eq("collection", pack)));

        MongoCursor<Document> cursor = collection.aggregate(aggregates).iterator();
        List<String> result = new ArrayList<>();

        try {
            while (cursor.hasNext()) {
                result.add(cursor.next().getString("word"));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public void learn(String ID, String contentRaw) {
        final List<String> tokens = Arrays.asList(contentRaw.split(" "));
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) saveWord(ID, tokens.get(i), "StopWord");
            else saveWord(ID, tokens.get(i), tokens.get(i + 1));
        }
    }

    private void saveWord(String pack, String word, String next) {
        MongoCollection<Document> collection = mongoDb.getCollection("word_pairs");
        collection.insertOne(new Document()
                .append("_id", new ObjectId())
                .append("word", word)
                .append("next", next)
                .append("collection", pack));
    }
}