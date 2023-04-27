package main.java.de.voidtech.alison.vader.util;

public enum SentimentModifyingTokens {
    NEVER("never"),
    SO("so"),
    THIS("this"),
    AT("at"),
    LEAST("least"),
    KIND("kind"),
    OF("of"),
    VERY("very"),
    BUT("but"),
    EXCLAMATION_MARK("!"),
    QUESTION_MARK("?"),
    CONTRACTION("n't");

    private final String value;

    SentimentModifyingTokens(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}