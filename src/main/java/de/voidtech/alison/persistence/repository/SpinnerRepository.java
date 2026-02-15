package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.Spinner;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpinnerRepository extends JpaRepository<Spinner, Long> {

    @Query("FROM Spinner WHERE isStillSpinning = true")
    List<Spinner> getSpinningSpinners();

    @Query("""
                FROM Spinner s
                WHERE s.serverID = :serverID
                ORDER BY
                  CASE
                    WHEN s.isStillSpinning = true
                      THEN (:nowMillis - s.spinnerStartTime)
                    ELSE (s.spinnerEndTime - s.spinnerStartTime)
                  END DESC
            """)
    List<Spinner> getSpinnerLeaderboardForServer(String serverID, long nowMillis, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Spinner s WHERE s.serverID = :serverID")
    long spinnerCountForServer(String serverID);

    @Query("FROM Spinner WHERE isStillSpinning = true AND serverID = :serverID")
    List<Spinner> revealSpinners(String serverID, Pageable pageable);

}