package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.TransientClaireWord;
import main.java.de.voidtech.alison.persistence.entity.PersistentClairePair;
import main.java.de.voidtech.alison.persistence.repository.ClairePairRepository;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.java.de.voidtech.alison.util.ResourceLoader.resourceAsString;

@Service
public class ClaireService {

    @Autowired
    private ClairePairRepository repository;

    @Value("classpath:ReallyCommonEnglish.txt")
    private Resource reallyCommonEnglishResource;

    //The most commonly used words in the english language. Source? Trust me bro
    private static List<String> ReallyCommonEnglish;

    @EventListener(ApplicationReadyEvent.class)
    void loadReallyCommonEnglish() {
        String commonEnglish = resourceAsString(reallyCommonEnglishResource);
        ReallyCommonEnglish = Arrays.asList(commonEnglish.split("\n"));
    }

    public String createReply(String prompt) {
        return createReply(prompt, AlisonService.CLAIRE_LENGTH);
    }

    public String createReply(String message, int length) {
        List<String> existingResponseSentences = getExistingResponseSentences(message);
        if (existingResponseSentences.isEmpty()) return "Huh";
        List<TransientClaireWord> tokenizedWords = new ArrayList<>();
        for (String response : existingResponseSentences) {
            tokenizedWords = Stream.concat(tokenizedWords.stream(),
                    stringToClaireWords(response).stream())
                    .collect(Collectors.toList());
        }
        String reply = createSentenceUnderLength(tokenizedWords, length);
        return reply == null ? "Huh" : reply;
    }

    public String createSentenceUnderLength(List<TransientClaireWord> words, int length) {
        if (words.isEmpty()) return null;
        StringBuilder result = new StringBuilder();
        TransientClaireWord next = getRandomStartWord(words);
        if (next == null) return null;
        while (next.isNotStopWord()) {
            if (result.length() + (next.getWord() + " ").length() > length) break;
            result.append(next.getWord()).append(" ");
            List<TransientClaireWord> potentials = getWordList(words, next.getNext());
            next = getRandomFromPotentials(potentials);
        }
        if (result.length() + next.getWord().length() <= length) result.append(next.getWord());
        return result.toString();
    }

    private TransientClaireWord getRandomFromPotentials(List<TransientClaireWord> potentials) {
        return potentials.get(new Random().nextInt(potentials.size()));
    }

    private TransientClaireWord getRandomStartWord(List<TransientClaireWord> words) {
        if (words.size() < 2) return null;
        else return words.get(new Random().nextInt(words.size() - 1));
    }

    private List<TransientClaireWord> getWordList(List<TransientClaireWord> words, String wordToFind) {
        return words.stream().filter(word -> word.getWord().equals(wordToFind)).collect(Collectors.toList());
    }

    private List<TransientClaireWord> stringToClaireWords(String content) {
        List<String> tokens = Arrays.asList(content.split(" "));
        List<TransientClaireWord> words = new ArrayList<>();
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) words.add(new TransientClaireWord(tokens.get(i), "StopWord"));
            else words.add(new TransientClaireWord(tokens.get(i), tokens.get(i + 1)));
        }
        return words;
    }

    private List<String> getExistingResponseSentences(String message) {
        List<String> words = filterOutPointlessContext(message);
        List<String> sentencePool = new ArrayList<>();
        for (String word : words) {
            List<PersistentClairePair> list = repository.getClairePairsContainingWord("%" + word.toLowerCase() + "%");
            for (PersistentClairePair pair : list) {
                sentencePool.add(pair.getReply());
            }
        }
        return sentencePool;
    }

    private List<String> filterOutPointlessContext(String message) {
        List<String> words = Arrays.asList(message.split(" "));
        List<String> filteredWords = words.stream()
                .filter(w -> !ReallyCommonEnglish.contains(w.toLowerCase().replaceAll("[^a-z A-Z]", "")))
                .collect(Collectors.toList());
        return filteredWords.isEmpty() ? words : filteredWords;
    }

    public long getConversationCount() {
        return repository.getConversationCount();
    }

    public void addMessages(Message message) {
        if (messageCanBeAdded(message)) {
            repository.save(new PersistentClairePair(message.getReferencedMessage().getContentDisplay(), message.getContentDisplay()));
        }
    }

    private boolean messageCanBeAdded(Message message) {
        return message.getReferencedMessage() != null
                && !message.getContentRaw().equals("")
                && !message.getReferencedMessage().getContentRaw().equals("");
    }
}