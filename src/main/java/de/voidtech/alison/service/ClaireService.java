package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.ClaireWord;
import main.java.de.voidtech.alison.entities.PersistentClairePair;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClaireService {

    @Autowired
    private SessionFactory sessionFactory;

    public String createReply(String prompt) {
        return createReply(prompt, TextGenerationService.CLAIRE_LENGTH);
    }

    public String createReply(String message, int length) {
        List<String> existingResponseSentences = getExistingResponseSentences(message);
        if (existingResponseSentences.isEmpty()) return "Huh";
        List<ClaireWord> tokenizedWords = new ArrayList<>();
        List<ClaireWord> finalWordList = new ArrayList<>();
        for (String response : existingResponseSentences) {
            tokenizedWords = Stream.concat(tokenizedWords.stream(),
                    stringToClaireWords(response).stream())
                    .collect(Collectors.toList());
        }
        String reply = createSentenceUnderLength(finalWordList, length);
        return reply == null ? "Huh" : reply;
    }

    public String createSentenceUnderLength(List<ClaireWord> words, int length) {
        if (words.isEmpty()) return null;
        StringBuilder result = new StringBuilder();
        ClaireWord next = getRandomStartWord(words);
        if (next == null) return null;
        while (next.isStopWord()) {
            if (result.length() + (next.getWord() + " ").length() > length) break;
            result.append(next.getWord()).append(" ");
            List<ClaireWord> potentials = getWordList(words, next.getNext());
            next = getRandomFromPotentials(potentials);
        }
        if (result.length() + next.getWord().length() <= length) result.append(next.getWord());
        return result.toString();
    }

    private ClaireWord getRandomFromPotentials(List<ClaireWord> potentials) {
        return potentials.get(new Random().nextInt(potentials.size()));
    }

    private ClaireWord getRandomStartWord(List<ClaireWord> words) {
        if (words.size() < 2) return null;
        else return words.get(new Random().nextInt(words.size() - 1));
    }

    private List<ClaireWord> getWordList(List<ClaireWord> words, String wordToFind) {
        return words.stream().filter(word -> word.getWord().equals(wordToFind)).collect(Collectors.toList());
    }

    private List<ClaireWord> stringToClaireWords(String content) {
        List<String> tokens = Arrays.asList(content.split(" "));
        List<ClaireWord> words = new ArrayList<>();
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) words.add(new ClaireWord(tokens.get(i), "StopWord"));
            else words.add(new ClaireWord(tokens.get(i), tokens.get(i + 1)));
        }
        return words;
    }

    @SuppressWarnings("unchecked")
    private List<String> getExistingResponseSentences(String message) {
        String[] words = message.split(" ");
        List<String> sentencePool = new ArrayList<>();
        for (String word : words) {
            try (Session session = sessionFactory.openSession()) {
                final List<PersistentClairePair> list = (List<PersistentClairePair>) session
                        .createQuery("FROM PersistentClairePair WHERE UPPER(message) ILIKE UPPER(%:word%)")
                        .setParameter("word", word)
                        .list();
                for (PersistentClairePair pair : list) {
                    sentencePool.add(pair.getReply());
                }
            }
        }
        return sentencePool;
    }

    public long getConversationCount() {
        try(Session session = sessionFactory.openSession())
        {
            @SuppressWarnings("rawtypes")
            Query query = session.createQuery("SELECT COUNT(*) FROM PersistentClairePair");
            long count = (long) query.uniqueResult();
            session.close();
            return count;
        }
    }

    public void addMessages(Message message) {
        if (messageCanBeAdded(message)) {
            try (Session session = sessionFactory.openSession()) {
                session.getTransaction().begin();
                session.saveOrUpdate(new PersistentClairePair(message.getReferencedMessage().getContentDisplay(), message.getContentDisplay()));
                session.getTransaction().commit();
            }
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