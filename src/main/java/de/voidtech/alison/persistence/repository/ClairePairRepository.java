package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.ClairePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClairePairRepository extends JpaRepository<ClairePair, Long> {

    @Query("FROM PersistentClairePair WHERE message LIKE :word")
    List<ClairePair> getClairePairsContainingWord(String word);

    @Query(value = "SELECT COUNT(*) FROM PersistentClairePair", nativeQuery = true)
    long getConversationCount();

}
