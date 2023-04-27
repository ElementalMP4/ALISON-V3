package main.java.de.voidtech.alison.vader.analyser;

public final class SentimentPolarities {

    private final float positivePolarity;
    private final float negativePolarity;
    private final float neutralPolarity;
    private final float compoundPolarity;
    private String pack;

    public SentimentPolarities(float positivePolarity, float negativePolarity, float neutralPolarity,
                               float compoundPolarity) {
        this.positivePolarity = positivePolarity;
        this.negativePolarity = negativePolarity;
        this.neutralPolarity = neutralPolarity;
        this.compoundPolarity = compoundPolarity;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getPack() {
        return this.pack;
    }

    public static SentimentPolarities emptySentimentState() {
        return new SentimentPolarities(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public float getPositivePolarity() {
        return positivePolarity;
    }
    public float getNegativePolarity() {
        return negativePolarity;
    }
    public float getNeutralPolarity() {
        return neutralPolarity;
    }
    public float getCompoundPolarity() {
        return compoundPolarity;
    }
}
