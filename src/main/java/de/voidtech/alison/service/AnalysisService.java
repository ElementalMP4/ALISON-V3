package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.Sentiment;
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
    private TextGenerationService textGenerationService;

    @Autowired
    private PrivacyService privacyService;

    private static final List<String> POSITIVE_EMOTES = Arrays.asList("❤", "🥰", "😘", "😄");
    private static final List<String> NEGATIVE_EMOTES = Arrays.asList("💔", "😔", "😭", "😢");

    public Sentiment analyseCollection(String pack) {
        if (!textGenerationService.dataIsAvailableForID(pack)) return null;
        String words = String.join(" ", textGenerationService.getAllWords(pack));
        Sentiment sentiment = analyseSentence(words);
        sentiment.setPack(pack);
        return sentiment;
    }

    public Sentiment analyseSentence(String sentence) {
        return new Sentiment(sentence);
    }

    public List<Sentiment> analyseServer(Guild guild) {
        List<Member> members = guild.loadMembers().get();
        List<Sentiment> sentiments = members.stream()
                .map(Member::getId)
                .filter(memberID -> !privacyService.userHasOptedOut(memberID))
                .filter(memberID -> textGenerationService.dataIsAvailableForID(memberID))
                .map(this::analyseCollection)
                .sorted(Comparator.comparing(sentiment -> sentiment != null ? sentiment.getAdjustedScore() : 0))
                .collect(Collectors.toList());
        Collections.reverse(sentiments);
        return sentiments;
    }

    public Sentiment averageSentiment(List<Sentiment> sentiments) {
        List<String> positives = sentiments.stream()
                .map(Sentiment::getPositives)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<String> negatives = sentiments.stream()
                .map(Sentiment::getNegatives)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        String original = sentiments.stream()
                .map(Sentiment::getOriginalString)
                .collect(Collectors.joining(" "));
        return new Sentiment(positives, negatives, original);
    }

    private String getRandomEmote(List<String> emotes) {
        return emotes.get(new Random().nextInt(emotes.size()));
    }

    public void respondToAlisonMention(Message message) {
        if (!message.getContentRaw().toLowerCase().contains("alison")) return;
        Sentiment sentiment = this.analyseSentence(message.getContentRaw().toLowerCase());
        if (sentiment.getAdjustedScore() >= 3) message.addReaction(getRandomEmote(POSITIVE_EMOTES)).queue();
        else if (sentiment.getAdjustedScore() <= -3) message.addReaction(getRandomEmote(NEGATIVE_EMOTES)).queue();
    }
}
