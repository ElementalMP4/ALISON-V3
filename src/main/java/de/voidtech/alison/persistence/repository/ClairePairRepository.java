package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.PersistentClairePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClairePairRepository extends JpaRepository<PersistentClairePair, Long> {

    @Query("FROM PersistentClairePair WHERE LOWER(message) LIKE LOWER(:word)")
    List<PersistentClairePair> getClairePairsContainingWord(String word);

    @Query(value = "SELECT COUNT(*) FROM claire_pairs", nativeQuery = true)
    long getConversationCount();
}