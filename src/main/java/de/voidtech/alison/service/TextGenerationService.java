package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.persistence.entity.AlisonWord;
import main.java.de.voidtech.alison.persistence.repository.AlisonWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TextGenerationService {

    @Autowired
    private AlisonWordRepository alisonWordRepository;

    public static final int CLAIRE_LENGTH = 1000;
    public static final int NICKNAME_LENGTH = 32;
    public static final int QUOTE_LENGTH = 100;
    public static final int SEARCH_LENGTH = 50;
    public static final int IMITATE_LENGTH = 2000;
    public static final int PROMPT_LENGTH = 35;

    private String generateMessage(String wordCollectionName, int length) {
        StringBuilder result = new StringBuilder();
        AlisonWord alisonWord = getRandomStartWord(wordCollectionName);
        if (alisonWord == null) return null;
        while (!alisonWord.isStopWord()) {
            if (result.length() + (alisonWord.getWord() + " ").length() > length) break;
            result.append(alisonWord.getWord()).append(" ");
            List<AlisonWord> potentials = getWordList(wordCollectionName, alisonWord.getNext());
            alisonWord = getRandomFromPotentials(potentials);
        }
        if (result.length() + alisonWord.getWord().length() <= length) result.append(alisonWord.getWord());
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

    private AlisonWord getRandomFromPotentials(List<AlisonWord> potentials) {
        return potentials.get(new Random().nextInt(potentials.size()));
    }

    public List<AlisonWord> getWordList(final String pack, final String word) {
        return alisonWordRepository.getAllWordsStartingWith(pack, word);
    }

    public AlisonWord getRandomStartWord(final String pack) {
        return alisonWordRepository.getRandomStartWord(pack);
    }

    public boolean dataIsAvailableForID(String id) {
        return getWordCountForCollection(id) > 0;
    }

    public long getWordCount() {
        return alisonWordRepository.getWordCount();
    }

    public long getModelCount() {
        return alisonWordRepository.getModelCount();
    }

    public long getWordCountForCollection(String id) {
        return alisonWordRepository.getWordCountInModel(id);
    }

    public void delete(String id) {
        alisonWordRepository.deleteModel(id);
    }

    public List<String> getAllWords(String pack) {
        List<AlisonWord> list = alisonWordRepository.getAllWordsInModel(pack);
        return list.stream().map(AlisonWord::getWord).collect(Collectors.toList());
    }

    public List<AlisonWord> getAllWordsNoPack() {
        return alisonWordRepository.getEverything();
    }

    public void learn(String ID, String contentRaw) {
        final List<String> tokens = Arrays.asList(contentRaw.split(" "));
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) saveWord(ID, tokens.get(i), "StopWord");
            else saveWord(ID, tokens.get(i), tokens.get(i + 1));
        }
    }

    private void saveWord(String id, String word, String next) {
        alisonWordRepository.save(new AlisonWord(id, word, next));
    }
}