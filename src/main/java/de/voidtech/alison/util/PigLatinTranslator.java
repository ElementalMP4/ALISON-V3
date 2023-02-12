package main.java.de.voidtech.alison.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PigLatinTranslator {

    private static final List<String> VOWELS = Arrays.asList("aeiou".split(""));
    private static final List<String> CONSONANTS = Arrays.asList("bcdfghjklmnpqrstvwxyz".split(""));

    public static String englishToPigLatin(String english) {
        List<String> words = Arrays.asList(english.split(" "));
        List<String> pigLatin = new ArrayList<>();

        for (String word : words) {
            if (CONSONANTS.contains(word.toLowerCase().substring(0, 1)) && VOWELS.contains(word.toLowerCase().substring(1, 2))) {
                pigLatin.add(convertWordStartingWithConsonantAndVowel(word));
            } else if (CONSONANTS.contains(word.toLowerCase().substring(0, 1)) && CONSONANTS.contains(word.toLowerCase().substring(1, 2))) {
                pigLatin.add(convertWordStartingWithTwoConsonants(word));
            } else if (VOWELS.contains(word.toLowerCase().substring(0, 1))) {
                pigLatin.add(word + "way");
            }
        }

        return String.join(" ", pigLatin);
    }

    private static String convertWordStartingWithTwoConsonants(String word) {
        String newWord = word;
        newWord = newWord.substring(2);
        newWord = newWord + word.substring(0, 2) + "ay";
        return newWord;
    }

    private static String convertWordStartingWithConsonantAndVowel(String word) {
        String newWord = word;
        newWord = newWord.substring(1);
        newWord = newWord + word.charAt(0) + "ay";
        return newWord;
    }
}
