package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.PersistentAlisonWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AlisonWordRepository extends JpaRepository<PersistentAlisonWord, Long> {

    @Query(value = "SELECT * FROM alison_words WHERE collection = :pack ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    PersistentAlisonWord getRandomStartWord(String pack);

    @Query("FROM PersistentAlisonWord WHERE collection = :pack AND word = :word")
    List<PersistentAlisonWord> getAllWordsStartingWith(String pack, String word);

    @Query(value = "SELECT COUNT(*) FROM alison_words", nativeQuery = true)
    long getWordCount();

    @Query(value = "SELECT COUNT(DISTINCT collection) FROM alison_words", nativeQuery = true)
    long getModelCount();

    @Query(value = "SELECT COUNT(*) FROM alison_words WHERE collection = :pack", nativeQuery = true)
    long getWordCountInModel(String pack);

    @Modifying
    @Transactional
    @Query("DELETE FROM PersistentAlisonWord WHERE collection = :pack")
    void deleteModel(String pack);

    @Query("FROM PersistentAlisonWord WHERE collection = :pack")
    List<PersistentAlisonWord> getAllWordsInModel(String pack);

    @Query("FROM PersistentAlisonWord")
    List<PersistentAlisonWord> getEverything();

}
