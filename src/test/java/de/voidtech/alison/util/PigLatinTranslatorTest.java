package test.java.de.voidtech.alison.util;

import main.java.de.voidtech.alison.util.PigLatinTranslator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PigLatinTranslatorTest {

    @Test
    public void englishToPigLatin_works_with_two_consonants() {
        String input = "chess change this that";
        String expectedOutput = "esschay angechay isthay atthay";
        String actualOutput = PigLatinTranslator.englishToPigLatin(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void englishToPigLatin_works_with_vowel_and_consonant() {
        String input = "carriage conform build package";
        String expectedOutput = "arriagecay onformcay uildbay ackagepay";
        String actualOutput = PigLatinTranslator.englishToPigLatin(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void englishToPigLatin_works_with_just_vowel() {
        String input = "england insert adjacent under";
        String expectedOutput = "englandway insertway adjacentway underway";
        String actualOutput = PigLatinTranslator.englishToPigLatin(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void englishToPigLatin_maintains_case() {
        String input = "CAPITALISED";
        String expectedOutput = "APITALISEDCay";
        String actualOutput = PigLatinTranslator.englishToPigLatin(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void englishToPigLatin_works_with_mixture() {
        String input = "this is a pig latin test lessgoo";
        String expectedOutput = "isthay isway away igpay atinlay esttay essgoolay";
        String actualOutput = PigLatinTranslator.englishToPigLatin(input);
        assertEquals(expectedOutput, actualOutput);
    }
}
