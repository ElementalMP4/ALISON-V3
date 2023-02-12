package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.PersistentAlisonWord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TextGenerationService {

    @Autowired
    private SessionFactory sessionFactory;

    public static final int CLAIRE_LENGTH = 1000;
    public static final int NICKNAME_LENGTH = 32;
    public static final int QUOTE_LENGTH = 100;
    public static final int SEARCH_LENGTH = 50;
    public static final int IMITATE_LENGTH = 2000;
    public static final int PROMPT_LENGTH = 35;

    private String generateMessage(String wordCollectionName, int length) {
        StringBuilder result = new StringBuilder();
        PersistentAlisonWord alisonWord = getRandomStartWord(wordCollectionName);
        if (alisonWord == null) return null;
        while (!alisonWord.isStopWord()) {
            if (result.length() + (alisonWord.getWord() + " ").length() > length) break;
            result.append(alisonWord.getWord()).append(" ");
            List<PersistentAlisonWord> potentials = getWordList(wordCollectionName, alisonWord.getNext());
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

    private PersistentAlisonWord getRandomFromPotentials(List<PersistentAlisonWord> potentials) {
        return potentials.get(new Random().nextInt(potentials.size()));
    }

    @SuppressWarnings("unchecked")
    public List<PersistentAlisonWord> getWordList(final String pack, final String word) {
        try (Session session = sessionFactory.openSession()) {
            final List<PersistentAlisonWord> list = (List<PersistentAlisonWord>) session
                    .createQuery("FROM PersistentAlisonWord WHERE collection = :pack AND word = :word")
                    .setParameter("pack", pack)
                    .setParameter("word", word)
                    .list();
            return list;
        }
    }

    public PersistentAlisonWord getRandomStartWord(final String pack) {
        try (Session session = sessionFactory.openSession()) {
            final PersistentAlisonWord alisonWord = (PersistentAlisonWord) session
                    .createQuery("FROM PersistentAlisonWord WHERE collection = :pack ORDER BY RANDOM()")
                    .setParameter("pack", pack)
                    .setMaxResults(1)
                    .uniqueResult();
            return alisonWord;
        }
    }

    public boolean dataIsAvailableForID(String id) {
        return getWordCountForCollection(id) > 0;
    }

    public long getWordCount() {
        try(Session session = sessionFactory.openSession())
        {
            @SuppressWarnings("rawtypes")
            Query query = session.createQuery("SELECT COUNT(*) FROM PersistentAlisonWord");
            long count = (long) query.uniqueResult();
            session.close();
            return count;
        }
    }

    public long getModelCount() {
        try(Session session = sessionFactory.openSession())
        {
            @SuppressWarnings("rawtypes")
            Query query = session.createQuery("SELECT COUNT(DISTINCT collection) FROM PersistentAlisonWord");
            long count = (long) query.uniqueResult();
            session.close();
            return count;
        }
    }

    public long getWordCountForCollection(String id) {
        try (Session session = sessionFactory.openSession()) {
            @SuppressWarnings("rawtypes")
            Query query = session
                    .createQuery("SELECT COUNT(*) FROM PersistentAlisonWord WHERE collection = :pack")
                    .setParameter("pack", id);
            long count = (long) query.uniqueResult();
            session.close();
            return count;
        }
    }

    public void delete(String id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM PersistentAlisonWord WHERE collection = :userID")
                    .setParameter("userID", id)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllWords(String pack) {
        try (Session session = sessionFactory.openSession()) {
            final List<PersistentAlisonWord> list = (List<PersistentAlisonWord>) session
                    .createQuery("FROM PersistentAlisonWord WHERE collection = :pack")
                    .setParameter("pack", pack)
                    .list();
            return list.stream().map(PersistentAlisonWord::getWord).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public List<PersistentAlisonWord> getAllWordsNoPack() {
        try (Session session = sessionFactory.openSession()) {
            return (List<PersistentAlisonWord>) session
                    .createQuery("FROM PersistentAlisonWord")
                    .list();
        }
    }

    public void learn(String ID, String contentRaw) {
        final List<String> tokens = Arrays.asList(contentRaw.split(" "));
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) saveWord(ID, tokens.get(i), "StopWord");
            else saveWord(ID, tokens.get(i), tokens.get(i + 1));
        }
    }

    private void saveWord(String id, String word, String next) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(new PersistentAlisonWord(id, word, next));
            session.getTransaction().commit();
        }
    }
}