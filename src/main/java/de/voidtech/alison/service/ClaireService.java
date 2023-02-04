package main.java.de.voidtech.alison.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import main.java.de.voidtech.alison.entities.AlisonWord;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.regex;

@Service
public class ClaireService {

    @Autowired
    private MongoDBService mongoDB;

    public String createReply(String prompt) {
        return createReply(prompt, TextGenerationService.CLAIRE_LENGTH);
    }

    public String createReply(String message, int length) {
        List<String> existingResponseSentences = getExistingResponseSentences(message);
        if (existingResponseSentences.isEmpty()) return "Huh";
        List<AlisonWord> tokenizedWords = new ArrayList<>();
        List<AlisonWord> finalWordList = new ArrayList<>();
        for (String response : existingResponseSentences) {
            tokenizedWords = Stream.concat(tokenizedWords.stream(),
                    stringToAlisonWords(response).stream())
                    .collect(Collectors.toList());
        }
        for (AlisonWord word : tokenizedWords) {
            AlisonWord wordInModel = finalWordList.stream()
                    .filter(w -> w.getWord().equals(word.getWord()) && w.getNext().equals(word.getNext()))
                    .findFirst()
                    .orElse(null);
            if (wordInModel == null) finalWordList.add(word);
            else wordInModel.incrementCount();
        }
        String reply = createProbableSentenceUnderLength(finalWordList, length);
        return reply == null ? "Huh" : reply;
    }

    public String createProbableSentenceUnderLength(List<AlisonWord> words, int length) {
        if (words.isEmpty()) return null;
        StringBuilder result = new StringBuilder();
        AlisonWord next = getRandomStartWord(words);
        if (next == null) return null;
        while (next.isStopWord()) {
            if (result.length() + (next.getWord() + " ").length() > length) break;
            result.append(next.getWord()).append(" ");
            List<AlisonWord> potentials = getWordList(words, next.getNext());
            next = getMostLikely(potentials);
        }
        if (result.length() + next.getWord().length() <= length) result.append(next.getWord());
        return result.toString();
    }

    private AlisonWord getMostLikely(List<AlisonWord> potentials) {
        return potentials.stream()
                .sorted(Comparator.comparing(AlisonWord::getFrequency))
                .collect(Collectors.toList()).get(0);
    }

    private AlisonWord getRandomStartWord(List<AlisonWord> words) {
        if (words.size() < 2) return null;
        else return words.get(new Random().nextInt(words.size() - 1));
    }

    private List<AlisonWord> getWordList(List<AlisonWord> words, String wordToFind) {
        return words.stream().filter(word -> word.getWord().equals(wordToFind)).collect(Collectors.toList());
    }

    private List<AlisonWord> stringToAlisonWords(String content) {
        List<String> tokens = Arrays.asList(content.split(" "));
        List<AlisonWord> words = new ArrayList<>();
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) words.add(new AlisonWord(tokens.get(i), "StopWord"));
            else words.add(new AlisonWord(tokens.get(i), tokens.get(i + 1)));
        }
        return words;
    }

    private List<String> getExistingResponseSentences(String message) {
        MongoCollection<Document> claireCollection = mongoDB.getCollection("claire_heap");
        List<String> words = Arrays.asList(message.split(" "));
        List<String> sentencePool = new ArrayList<>();
        for (String word : words) {
            MongoCursor<Document> documentCursor = claireCollection.find(regex("message", word)).iterator();
            while (documentCursor.hasNext()) {
                sentencePool.add(documentCursor.next().getString("reply"));
            }
        }
        return sentencePool;
    }

    public long getConversationCount() {
        MongoCollection<Document> claireCollection = mongoDB.getCollection("claire_heap");
        return claireCollection.countDocuments();
    }

    public void addMessages(Message message) {
        if (messageCanBeAdded(message)) {
            MongoCollection<Document> claireCollection = mongoDB.getCollection("claire_heap");
            claireCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("message", message.getReferencedMessage().getContentRaw().replaceAll("@", "``@``"))
                    .append("reply", message.getContentRaw().replaceAll("@", "``@``")));
        }
    }

    private boolean messageCanBeAdded(Message message) {
        return message.getReferencedMessage() != null
                && !message.getContentRaw().equals("")
                && !message.getReferencedMessage().getContentRaw().equals("");
    }

    public void replyToMessage(Message message) {
        if (message.getMentionedUsers().contains(message.getJDA().getSelfUser())
                | message.getChannel().getType().equals(ChannelType.PRIVATE)) {
            message.reply(createReply(message.getContentDisplay())).mentionRepliedUser(false).queue();
        }
    }
}