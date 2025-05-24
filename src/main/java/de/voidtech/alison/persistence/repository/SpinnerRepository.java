package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.Spinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpinnerRepository extends JpaRepository<Spinner, Long> {
    @Query("FROM Spinner WHERE isStillSpinning = true")
    List<Spinner> getSpinningSpinners();
}