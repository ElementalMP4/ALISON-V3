package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.IgnoredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface IgnoredUserRepository extends JpaRepository<IgnoredUser, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM IgnoredUser WHERE userID = :userID")
    void deleteByUserId(String userID);

    @Query("FROM IgnoredUser WHERE userID = :userID")
    IgnoredUser getUserById(String userID);
}
