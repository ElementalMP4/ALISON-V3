package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.AlisonWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AlisonWordRepository extends JpaRepository<AlisonWord, Long> {

    @Query(value = "SELECT * FROM AlisonWord WHERE collection = :pack ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    AlisonWord getRandomStartWord(String pack);

    @Query("FROM AlisonWord WHERE collection = :pack AND word = :word")
    List<AlisonWord> getAllWordsStartingWith(String pack, String word);

    @Query(value = "SELECT COUNT(*) FROM AlisonWord", nativeQuery = true)
    long getWordCount();

    @Query(value = "SELECT COUNT(DISTINCT collection) FROM AlisonWord", nativeQuery = true)
    long getModelCount();

    @Query(value = "SELECT COUNT(*) FROM AlisonWord WHERE collection = :pack", nativeQuery = true)
    long getWordCountInModel(String pack);

    @Modifying
    @Transactional
    @Query("DELETE FROM AlisonWord WHERE collection = :pack")
    void deleteModel(String pack);

    @Query("FROM AlisonWord WHERE collection = :pack")
    List<AlisonWord> getAllWordsInModel(String pack);

    @Query("FROM AlisonWord")
    List<AlisonWord> getEverything();

}
