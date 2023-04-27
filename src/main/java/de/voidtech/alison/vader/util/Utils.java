package main.java.de.voidtech.alison.vader.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public final class Utils {

    public static final Set<String> PUNCTUATIONS = ImmutableSet.of(".", "!", "?", ",", ";", ":", "-", "'",
            "\"", "!!", "!!!", "??", "???", "?!?", "!?!", "?!?!", "!?!?");

    public static final Set<String> NEGATIVE_WORDS =
            ImmutableSet.of("aint", "arent", "cannot", "cant", "couldnt", "darent", "didnt", "doesnt",
                    "ain't", "aren't", "can't", "couldn't", "daren't", "didn't", "doesn't", "dont", "hadnt",
                    "hasnt", "havent", "isnt", "mightnt", "mustnt", "neither", "don't", "hadn't", "hasn't",
                    "haven't", "isn't", "mightn't", "mustn't", "neednt", "needn't", "never", "none", "nope",
                    "nor", "not", "nothing", "nowhere", "oughtnt", "shant", "shouldnt", "uhuh", "wasnt",
                    "werent", "oughtn't", "shan't", "shouldn't", "uh-uh", "wasn't", "weren't", "without",
                    "wont", "wouldnt", "won't", "wouldn't", "rarely", "seldom", "despite");

    public static final Map<String, Float> BOOSTER_DICTIONARY = ImmutableMap.<String, Float>builder()
            .put("decidedly", Valence.DEFAULT_BOOSTING.getValue())
            .put("uber", Valence.DEFAULT_BOOSTING.getValue())
            .put("barely", Valence.DEFAULT_DAMPING.getValue())
            .put("particularly", Valence.DEFAULT_BOOSTING.getValue())
            .put("enormously", Valence.DEFAULT_BOOSTING.getValue())
            .put("less", Valence.DEFAULT_DAMPING.getValue())
            .put("absolutely", Valence.DEFAULT_BOOSTING.getValue())
            .put("kinda", Valence.DEFAULT_DAMPING.getValue())
            .put("flipping", Valence.DEFAULT_BOOSTING.getValue())
            .put("awfully", Valence.DEFAULT_BOOSTING.getValue())
            .put("purely", Valence.DEFAULT_BOOSTING.getValue())
            .put("majorly", Valence.DEFAULT_BOOSTING.getValue())
            .put("substantially", Valence.DEFAULT_BOOSTING.getValue())
            .put("partly", Valence.DEFAULT_DAMPING.getValue())
            .put("remarkably", Valence.DEFAULT_BOOSTING.getValue())
            .put("really", Valence.DEFAULT_BOOSTING.getValue())
            .put("sort of", Valence.DEFAULT_DAMPING.getValue())
            .put("little", Valence.DEFAULT_DAMPING.getValue())
            .put("fricking", Valence.DEFAULT_BOOSTING.getValue())
            .put("sorta", Valence.DEFAULT_DAMPING.getValue())
            .put("amazingly", Valence.DEFAULT_BOOSTING.getValue())
            .put("kind of", Valence.DEFAULT_DAMPING.getValue())
            .put("just enough", Valence.DEFAULT_DAMPING.getValue())
            .put("fucking", Valence.DEFAULT_BOOSTING.getValue())
            .put("occasionally", Valence.DEFAULT_DAMPING.getValue())
            .put("somewhat", Valence.DEFAULT_DAMPING.getValue())
            .put("kindof", Valence.DEFAULT_DAMPING.getValue())
            .put("friggin", Valence.DEFAULT_BOOSTING.getValue())
            .put("incredibly", Valence.DEFAULT_BOOSTING.getValue())
            .put("totally", Valence.DEFAULT_BOOSTING.getValue())
            .put("marginally", Valence.DEFAULT_DAMPING.getValue())
            .put("more", Valence.DEFAULT_BOOSTING.getValue())
            .put("considerably", Valence.DEFAULT_BOOSTING.getValue())
            .put("fabulously", Valence.DEFAULT_BOOSTING.getValue())
            .put("hardly", Valence.DEFAULT_DAMPING.getValue())
            .put("very", Valence.DEFAULT_BOOSTING.getValue())
            .put("sortof", Valence.DEFAULT_DAMPING.getValue())
            .put("kind-of", Valence.DEFAULT_DAMPING.getValue())
            .put("scarcely", Valence.DEFAULT_DAMPING.getValue())
            .put("thoroughly", Valence.DEFAULT_BOOSTING.getValue())
            .put("quite", Valence.DEFAULT_BOOSTING.getValue())
            .put("most", Valence.DEFAULT_BOOSTING.getValue())
            .put("completely", Valence.DEFAULT_BOOSTING.getValue())
            .put("frigging", Valence.DEFAULT_BOOSTING.getValue())
            .put("intensely", Valence.DEFAULT_BOOSTING.getValue())
            .put("utterly", Valence.DEFAULT_BOOSTING.getValue())
            .put("highly", Valence.DEFAULT_BOOSTING.getValue())
            .put("extremely", Valence.DEFAULT_BOOSTING.getValue())
            .put("unbelievably", Valence.DEFAULT_BOOSTING.getValue())
            .put("almost", Valence.DEFAULT_DAMPING.getValue())
            .put("especially", Valence.DEFAULT_BOOSTING.getValue())
            .put("fully", Valence.DEFAULT_BOOSTING.getValue())
            .put("frickin", Valence.DEFAULT_BOOSTING.getValue())
            .put("tremendously", Valence.DEFAULT_BOOSTING.getValue())
            .put("exceptionally", Valence.DEFAULT_BOOSTING.getValue())
            .put("flippin", Valence.DEFAULT_BOOSTING.getValue())
            .put("hella", Valence.DEFAULT_BOOSTING.getValue())
            .put("so", Valence.DEFAULT_BOOSTING.getValue())
            .put("greatly", Valence.DEFAULT_BOOSTING.getValue())
            .put("hugely", Valence.DEFAULT_BOOSTING.getValue())
            .put("deeply", Valence.DEFAULT_BOOSTING.getValue())
            .put("unusually", Valence.DEFAULT_BOOSTING.getValue())
            .put("entirely", Valence.DEFAULT_BOOSTING.getValue())
            .put("slightly", Valence.DEFAULT_DAMPING.getValue())
            .put("effing", Valence.DEFAULT_BOOSTING.getValue())
            .build();

    public static final Map<String, Float> SENTIMENT_LADEN_IDIOMS_VALENCE_DICTIONARY =
            ImmutableMap.<String, Float>builder()
                    .put("cut the mustard", 2f)
                    .put("bad ass", 1.5f)
                    .put("kiss of death", -1.5f)
                    .put("yeah right", -2f)
                    .put("the bomb", 3f)
                    .put("hand to mouth", -2f)
                    .put("the shit", 3f)
                    .build();
    public static Map<String, Float> WORD_VALENCE_DICTIONARY;

    public static boolean isUpper(String token) {
        if (StringUtils.startsWithIgnoreCase(token, Constants.HTTP_URL_PREFIX)) {
            return false;
        }
        if (StringUtils.startsWithIgnoreCase(token, Constants.HTTPS_URL_PREFIX)) {
            return false;
        }
        if (!Constants.NON_NUMERIC_STRING_REGEX.matcher(token).matches()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            if (Character.isLowerCase(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static {
        final InputStream lexFile = Utils.class.getClassLoader().getResourceAsStream("vader-lexicon.txt");
        final Map<String, Float> lexDictionary = new HashMap<>();
        if (lexFile != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(lexFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] lexFileData = line.split("\\t");
                    final String currentText = lexFileData[0];
                    final Float currentTextValence = Float.parseFloat(lexFileData[1]);
                    lexDictionary.put(currentText, currentTextValence);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        WORD_VALENCE_DICTIONARY = Collections.unmodifiableMap(lexDictionary);
    }
}
