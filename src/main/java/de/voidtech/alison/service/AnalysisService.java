package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.Sentiment;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {
    public Sentiment analyseCollection(String id) {
        return new Sentiment(null, null, null);
    }

    public List<Sentiment> analyseServer(Guild guild) {
        return new ArrayList<>();
    }

    public Sentiment averageSentiment(List<Sentiment> everyMemberJudgedIntensely) {
        return new Sentiment(null, null, null);
    }
}
