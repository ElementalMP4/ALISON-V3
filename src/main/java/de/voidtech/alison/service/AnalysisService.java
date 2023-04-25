package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.AfinnWord;
import main.java.de.voidtech.alison.entities.Sentiment;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static main.java.de.voidtech.alison.util.ResourceLoader.resourceAsString;

@Service
public class AnalysisService {

    @Autowired
    private AlisonService textGenerationService;

    @Autowired
    private PrivacyService privacyService;

    @Value("classpath:AFINN.txt")
    private Resource afinnResource;

    private static final List<String> POSITIVE_EMOTES = Arrays.asList("❤", "🥰", "😘", "😄");
    private static final List<String> NEGATIVE_EMOTES = Arrays.asList("💔", "😔", "😭", "😢");

    private static List<AfinnWord> AfinnWords = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    void loadAfinnOnBoot() {
        String afinnData = resourceAsString(afinnResource);
        String[] lines = afinnData.split("\n");
        for (String line : lines) {
            int score = Integer.parseInt(line.substring(line.length() - 2).trim());
            String text = line.substring(0, line.length() - 2).trim();
            AfinnWords.add(new AfinnWord(text, score));
        }
    }

    public Sentiment analyseCollection(String pack) {
        if (!textGenerationService.dataIsAvailableForID(pack)) return null;
        String words = String.join(" ", textGenerationService.getAllWords(pack));
        Sentiment sentiment = analyseSentence(words);
        sentiment.setPack(pack);
        return sentiment;
    }

    private List<String> tokenise(String input) {
        return Arrays.stream(input.toLowerCase().split(" "))
                .map(i -> i.replaceAll("([^a-zA-Z])", ""))
                .collect(Collectors.toList());
    }

    public Sentiment analyseSentence(String input) {
        List<String> words = tokenise(input);
        List<AfinnWord> wordsWithScores = new ArrayList<>();
        AfinnWords.forEach(word -> {
            if (words.contains(word.getWord())) {
                for (int i = 0; i < Collections.frequency(words, word.getWord()); i++) {
                    wordsWithScores.add(word);
                }
            }
        });
        List<AfinnWord> positives = new ArrayList<>();
        List<AfinnWord> negatives = new ArrayList<>();
        for (AfinnWord word : wordsWithScores) {
            if (word.getScore() < 0) negatives.add(word);
            else positives.add(word);
        }
        return new Sentiment(positives, negatives, input);
    }

    public List<Sentiment> analyseServer(Guild guild) {
        List<Member> members = guild.loadMembers().get();
        List<Sentiment> sentiments = members.stream()
                .map(Member::getId)
                .filter(memberID -> !privacyService.userHasOptedOut(memberID))
                .filter(memberID -> textGenerationService.dataIsAvailableForID(memberID))
                .map(this::analyseCollection)
                .sorted(Comparator.comparing(sentiment -> sentiment != null ? sentiment.getScore() : 0))
                .collect(Collectors.toList());
        Collections.reverse(sentiments);
        return sentiments;
    }

    public Sentiment averageSentiment(List<Sentiment> sentiments) {
        List<AfinnWord> positives = sentiments.stream()
                .map(Sentiment::getPositives)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<AfinnWord> negatives = sentiments.stream()
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
        if (sentiment.getScore() >= 2) message.addReaction(getRandomEmote(POSITIVE_EMOTES)).queue();
        else if (sentiment.getScore() <= -2) message.addReaction(getRandomEmote(NEGATIVE_EMOTES)).queue();
    }
}