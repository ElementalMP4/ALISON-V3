package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.persistence.entity.IgnoredChannel;
import main.java.de.voidtech.alison.persistence.entity.IgnoredUser;
import main.java.de.voidtech.alison.persistence.repository.IgnoredChannelRepository;
import main.java.de.voidtech.alison.persistence.repository.IgnoredUserRepository;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivacyService {

    @Autowired
    private IgnoredChannelRepository ignoredChannelRepository;

    @Autowired
    private IgnoredUserRepository ignoredUserRepository;

    public void optOut(String id) {
        ignoredUserRepository.save(new IgnoredUser(id));
    }

    public void optIn(String id) {
        ignoredUserRepository.deleteByUserId(id);
    }

    public boolean userHasOptedOut(String id) {
        IgnoredUser user = ignoredUserRepository.getUserById(id);
        return user != null;
    }

    public boolean channelIsIgnored(Message message) {
        if (message.getChannel().getType().equals(ChannelType.PRIVATE)) return false;
        else return channelIsIgnored(message.getChannel().getId(), message.getGuild().getId());
    }

    public boolean channelIsIgnored(String channelID, String guildID) {
        IgnoredChannel channel = ignoredChannelRepository.getChannelByChannelIdAndGuildId(channelID, guildID);
        return channel != null;
    }

    public List<String> getIgnoredChannelsForServer(String guildID) {
        List<IgnoredChannel> list = ignoredChannelRepository.getAllIgnoredChannelsForServer(guildID);
        return list.stream().map(IgnoredChannel::getChannel).collect(Collectors.toList());
    }

    public void ignoreChannel(String channelID, String guildID) {
       ignoredChannelRepository.save(new IgnoredChannel(channelID, guildID));
    }

    public void unignoreChannel(String channelID, String guildID) {
        ignoredChannelRepository.deleteByChannelIdAndGuildId(channelID, guildID);
    }
}