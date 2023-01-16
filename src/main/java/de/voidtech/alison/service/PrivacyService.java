package main.java.de.voidtech.alison.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrivacyService {
    public boolean userHasOptedOut(String id) {
        return false;
    }

    public boolean channelIsIgnored(String channelId, String guildId) {
        return false;
    }

    public List<String> getIgnoredChannelsForServer(String id) {
        return new ArrayList<>();
    }

    public void ignoreChannel(String channelID, String id) {
    }

    public void unignoreChannel(String channelID, String id) {
    }

    public void optIn(String id) {
    }

    public void optOut(String id) {
    }
}
