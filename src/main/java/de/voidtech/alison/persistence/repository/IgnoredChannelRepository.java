package main.java.de.voidtech.alison.persistence.repository;

import main.java.de.voidtech.alison.persistence.entity.IgnoredChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface IgnoredChannelRepository extends JpaRepository<IgnoredChannel, Long> {

    @Query("FROM IgnoredChannel WHERE channelID = :channelID AND guildID = :guildID")
    IgnoredChannel getChannelByChannelIdAndGuildId(String channelID, String guildID);

    @Query("FROM IgnoredChannel WHERE guildID = :guildID")
    List<IgnoredChannel> getAllIgnoredChannelsForServer(String guildID);

    @Modifying
    @Transactional
    @Query("DELETE FROM IgnoredChannel WHERE channelID = :channelID AND guildID = :guildID")
    void deleteByChannelIdAndGuildId(String channelID, String guildID);
}
