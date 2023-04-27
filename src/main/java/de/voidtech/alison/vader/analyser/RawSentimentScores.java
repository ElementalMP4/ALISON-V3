package main.java.de.voidtech.alison.vader.analyser;

public final class RawSentimentScores {

    private final float positiveScore;
    private final float negativeScore;
    private final float neutralScore;

    public RawSentimentScores(float positiveScore, float negativeScore, float neutralScore) {
        this.positiveScore = positiveScore;
        this.negativeScore = negativeScore;
        this.neutralScore = neutralScore;
    }

    public float getPositiveScore() {
        return this.positiveScore;
    }

    public float getNegativeScore() {
        return this.negativeScore;
    }

    public float getNeutralScore() {
        return this.neutralScore;
    }
}
