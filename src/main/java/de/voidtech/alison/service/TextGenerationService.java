package main.java.de.voidtech.alison.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
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
            word = getRandomDocumentField(potentials, "word");
            nextWord = getRandomDocumentField(potentials, "next");
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

    private String getRandomDocumentField(List<Document> potentials, String field) {
        return potentials.get(new Random().nextInt(potentials.size())).getString(field);
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
        return true;
    }

    public long getWordCount() {
        return 0;
    }

    public long getModelCount() {
        return 0;
    }

    public Map<String, Long> getTopFiveWords(String id) {
        return new HashMap<>();
    }

    public long getWordCountForCollection(String id) {
        return 0;
    }

    public void delete(String id) {
    }
}
