package main.java.de.voidtech.alison.entities;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sentiment {
    private final List<AfinnWord> positives;
    private final List<AfinnWord> negatives;
    private final String originalWords;
    private final List<AfinnWord> tokens;
    private final int score;
    private String pack;

    public Sentiment(List<AfinnWord> positives, List<AfinnWord> negatives, String originalWords) {
        this.positives = positives;
        this.negatives = negatives;
        this.score = addAfinnScores(positives) + addAfinnScores(negatives);
        this.originalWords = originalWords;
        this.tokens = Stream.concat(this.positives.stream(), this.negatives.stream()).collect(Collectors.toList());
    }

    private int addAfinnScores(List<AfinnWord> list) {
        return list.stream().map(AfinnWord::getScore).reduce(0, Integer::sum);
    }

    public double getAverageScore() {
        if (this.tokens.size() == 0) return 0;
        return (double)score / (double)tokens.size();
    }

    public int getNegativeCount() {
        return this.negatives.size();
    }

    public int getPositiveCount() {
        return this.positives.size();
    }

    public int getScore() {
        return this.score;
    }

    public int getTokenCount() {
        return this.tokens.size();
    }

    public int getTotalWordCount() {
        return this.originalWords.split(" ").length;
    }

    public List<AfinnWord> getPositives() {
        return this.positives;
    }

    public List<AfinnWord> getNegatives() {
        return this.negatives;
    }

    public String getOriginalString() {
        return this.originalWords;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getPack() {
        return this.pack;
    }
}