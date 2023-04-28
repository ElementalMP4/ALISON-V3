package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.vader.analyser.SentimentAnalyser;
import main.java.de.voidtech.alison.vader.analyser.SentimentPolarities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    @Autowired
    private AlisonService textGenerationService;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private ReallyCommonEnglishService reallyCommonEnglishService;

    private static final List<String> POSITIVE_EMOTES = Arrays.asList("❤", "🥰", "😘", "😄");
    private static final List<String> NEGATIVE_EMOTES = Arrays.asList("💔", "😔", "😭", "😢");

    public SentimentPolarities analyseCollection(String pack) {
        if (!textGenerationService.dataIsAvailableForID(pack)) return null;
        List<String> words = textGenerationService.getAllWords(pack);
        String everythingAllTogether = String.join(" ", reallyCommonEnglishService.filterOutPointlessContext(words));
        SentimentPolarities sentiment = analyseSentence(everythingAllTogether);
        sentiment.setPack(pack);
        return sentiment;
    }

    public SentimentPolarities analyseSentence(String input) {
        return SentimentAnalyser.getScoresFor(input);
    }

    public List<SentimentPolarities> analyseServer(Guild guild) {
        List<Member> members = guild.loadMembers().get();
        List<SentimentPolarities> sentiments = members.stream()
                .map(Member::getId)
                .filter(memberID -> !privacyService.userHasOptedOut(memberID))
                .filter(memberID -> textGenerationService.dataIsAvailableForID(memberID))
                .map(this::analyseCollection)
                .sorted(Comparator.comparing(sentiment -> sentiment != null ? sentiment.getCompoundPolarity() : 0))
                .collect(Collectors.toList());
        Collections.reverse(sentiments);
        return sentiments;
    }

    public SentimentPolarities averageSentiment(List<SentimentPolarities> sentiments) {
        return SentimentAnalyser.averageListOfSentiments(sentiments);
    }

    private String getRandomEmote(List<String> emotes) {
        return emotes.get(new Random().nextInt(emotes.size()));
    }

    public void respondToAlisonMention(Message message) {
        if (!message.getContentRaw().toLowerCase().contains("alison")) return;

        SentimentPolarities polarity = SentimentAnalyser.getScoresFor(message.getContentRaw());

        if (polarity.getNeutralPolarity() > polarity.getNegativePolarity()
                && polarity.getNeutralPolarity() > polarity.getPositivePolarity()) return;

        if (polarity.getPositivePolarity() > polarity.getNegativePolarity()) message.addReaction(getRandomEmote(POSITIVE_EMOTES)).queue();
        else if (polarity.getPositivePolarity() < polarity.getNegativePolarity()) message.addReaction(getRandomEmote(NEGATIVE_EMOTES)).queue();
    }
}