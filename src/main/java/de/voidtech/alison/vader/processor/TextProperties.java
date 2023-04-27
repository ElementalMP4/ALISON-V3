package main.java.de.voidtech.alison.vader.processor;

import main.java.de.voidtech.alison.vader.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TextProperties {

    private final String inputText;
    private List<String> wordsAndEmoticons;
    private Set<String> wordsOnly;
    private boolean hasYellWords;

    public TextProperties(final String inputText) throws IOException {
        this.inputText = inputText;
        setWordsAndEmoticons();
        setHasYellWords(hasCapDifferential(getWordsAndEmoticons()));
    }

    private List<String> tokensAftersKeepingEmoticons(final String unTokenizedText,
                                                      final Set<String> tokensWithoutPunctuations) throws IOException {
        final List<String> wordsAndEmoticonsList = new ArrayList<>();
        new InputAnalyser().keepPunctuation(unTokenizedText, wordsAndEmoticonsList::add);
        wordsAndEmoticonsList.replaceAll(t -> stripPunctuations(t, tokensWithoutPunctuations));
        return wordsAndEmoticonsList;
    }

    private String stripPunctuations(String token, Set<String> tokensWithoutPunctuations) {
        for (final String punct : Utils.PUNCTUATIONS) {
            if (token.startsWith(punct)) {
                final String strippedToken = token.substring(punct.length());
                if (tokensWithoutPunctuations.contains(strippedToken)) {
                    return strippedToken;
                }
            } else if (token.endsWith(punct)) {
                final String strippedToken = token.substring(0, token.length() - punct.length());
                if (tokensWithoutPunctuations.contains(strippedToken)) {
                    return strippedToken;
                }
            }
        }
        return token;
    }

    private void setWordsAndEmoticons() throws IOException {
        setWordsOnly();
        this.wordsAndEmoticons = tokensAftersKeepingEmoticons(inputText, wordsOnly);
    }

    private void setWordsOnly() throws IOException {
        this.wordsOnly = new HashSet<>();
        new InputAnalyser().removePunctuation(inputText, wordsOnly::add);
    }

    public List<String> getWordsAndEmoticons() {
        return wordsAndEmoticons;
    }

    public boolean isYelling() {
        return hasYellWords;
    }

    private void setHasYellWords(boolean hasYellWords) {
        this.hasYellWords = hasYellWords;
    }

    private boolean hasCapDifferential(List<String> tokenList) {
        int countAllCaps = 0;
        for (String token : tokenList) {
            if (Utils.isUpper(token)) {
                countAllCaps++;
            }
        }
        final int capDifferential = tokenList.size() - countAllCaps;
        return (capDifferential > 0) && (capDifferential < tokenList.size());
    }
}