package main.java.de.voidtech.alison.vader.analyser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import main.java.de.voidtech.alison.vader.processor.TextProperties;
import main.java.de.voidtech.alison.vader.util.Constants;
import main.java.de.voidtech.alison.vader.util.SentimentModifyingTokens;
import main.java.de.voidtech.alison.vader.util.Utils;
import main.java.de.voidtech.alison.vader.util.Valence;
import org.apache.commons.lang3.StringUtils;

public class SentimentAnalyser {

    public static SentimentPolarities getScoresFor(String inputString) {
        return computeSentimentPolaritiesFor(inputString);
    }

    public static SentimentPolarities averageListOfSentiments(List<SentimentPolarities> sentiments) {
        List<Float> positives = sentiments.stream().map(SentimentPolarities::getPositivePolarity).collect(Collectors.toList());
        List<Float> neutrals = sentiments.stream().map(SentimentPolarities::getNeutralPolarity).collect(Collectors.toList());
        List<Float> negatives = sentiments.stream().map(SentimentPolarities::getNegativePolarity).collect(Collectors.toList());
        List<Float> compounds = sentiments.stream().map(SentimentPolarities::getCompoundPolarity).collect(Collectors.toList());

        float positiveAverage = average(positives);
        float neutralAverage = average(neutrals);
        float negativeAverage = average(negatives);
        float compoundAverage = average(compounds);

        return new SentimentPolarities(positiveAverage, negativeAverage, neutralAverage, compoundAverage);
    }

    private static float average(List<Float> floats) {
        return (float) floats.stream().mapToDouble(Float::doubleValue).sum() / floats.size();
    }

    private static float adjustValenceIfCapital(final String precedingToken, final float currentValence, final boolean inputHasYelling) {
        float scalar = 0.0F;
        final String precedingTokenLower = precedingToken.toLowerCase();
        if (Utils.BOOSTER_DICTIONARY.containsKey(precedingTokenLower)) {
            scalar = Utils.BOOSTER_DICTIONARY.get(precedingTokenLower);
            if (currentValence < 0.0F) {
                scalar = -scalar;
            }
            if (Utils.isUpper(precedingToken) && inputHasYelling) {
                if (currentValence > 0.0F) {
                    scalar += Valence.ALL_CAPS_FACTOR.getValue();
                } else {
                    scalar -= Valence.ALL_CAPS_FACTOR.getValue();
                }
            }
        }
        return scalar;
    }

    private static boolean areNeverPhrasesPresent(final int distance, final int currentItemPosition,
                                                  final List<String> wordsAndEmoticons) {
        if (distance == 1) {
            final String wordAtDistanceTwoLeft =
                    wordsAndEmoticons.get(currentItemPosition - Constants.PRECEDING_BIGRAM_WINDOW);
            final String wordAtDistanceOneLeft =
                    wordsAndEmoticons.get(currentItemPosition - Constants.PRECEDING_UNIGRAM_WINDOW);
            return (wordAtDistanceTwoLeft.equals(SentimentModifyingTokens.NEVER.getValue()))
                    && (wordAtDistanceOneLeft.equals(SentimentModifyingTokens.SO.getValue())
                    || (wordAtDistanceOneLeft.equals(SentimentModifyingTokens.NEVER.getValue())));
        } else if (distance == 2) {
            final String wordAtDistanceThreeLeft = wordsAndEmoticons.get(currentItemPosition
                    - Constants.PRECEDING_TRIGRAM_WINDOW);
            final String wordAtDistanceTwoLeft =
                    wordsAndEmoticons.get(currentItemPosition - Constants.PRECEDING_BIGRAM_WINDOW);
            final String wordAtDistanceOneLeft =
                    wordsAndEmoticons.get(currentItemPosition - Constants.PRECEDING_UNIGRAM_WINDOW);
            return (wordAtDistanceThreeLeft.equals(SentimentModifyingTokens.NEVER.getValue()))
                    && (wordAtDistanceTwoLeft.equals(SentimentModifyingTokens.SO.getValue())
                    || wordAtDistanceTwoLeft.equals(SentimentModifyingTokens.THIS.getValue()))
                    || (wordAtDistanceOneLeft.equals(SentimentModifyingTokens.SO.getValue())
                    || wordAtDistanceOneLeft.equals(SentimentModifyingTokens.THIS.getValue()));
        }
        return false;
    }

    private static float dampValenceIfNegativeTokensFound(final float currentValence, final int distance,
                                                          final int currentItemPosition, final int closeTokenIndex,
                                                          final List<String> wordsAndEmoticons) {
        float newValence = currentValence;
        final boolean anyNeverPhrase = areNeverPhrasesPresent(distance, currentItemPosition, wordsAndEmoticons);

        if (!anyNeverPhrase) {
            if (isNegative(wordsAndEmoticons.get(closeTokenIndex))) {
                newValence *= Valence.NEGATIVE_WORD_DAMPING_FACTOR.getValue();
            }
        } else {
            final float neverPhraseAdjustment = (distance == 1)
                    ? Valence.PRECEDING_BIGRAM_HAVING_NEVER_DAMPING_FACTOR.getValue()
                    : Valence.PRECEDING_TRIGRAM_HAVING_NEVER_DAMPING_FACTOR.getValue();
            newValence *= neverPhraseAdjustment;
        }

        return newValence;
    }

    private static List<String> getLeftGrams(final List<String> tokenList, final int minGramLength,
                                             final int maxGramLength, final int startPosition,
                                             final int maxDistanceFromStartPosition) {
        Preconditions.checkArgument(minGramLength > 0 && maxGramLength > 0,
                "Left Gram lengths should not be negative or zero.");
        Preconditions.checkArgument(maxGramLength >= minGramLength,
                "Maximum left gram length should be at least equal to the minimum value.");
        Preconditions.checkArgument(tokenList != null);

        final int noOfTokens = tokenList.size();
        if (noOfTokens < minGramLength) {
            return Collections.emptyList();
        }

        final List<String> result = new ArrayList<>();
        for (int end = startPosition; end > 0; end--) {
            final int windowStart = end - minGramLength + 1;
            final int windowEnd = end - maxGramLength;
            String currentSuffix = tokenList.get(end);
            for (int start = windowStart; start >= ((windowEnd < 0) ? 0 : Math.max(0, windowEnd) + 1); start--) {
                currentSuffix = tokenList.get(start) + Constants.SPACE_SEPARATOR + currentSuffix;
                result.add(currentSuffix);
                if ((startPosition - end) == maxDistanceFromStartPosition) {
                    return result;
                }
            }
        }
        return result;
    }

    private static List<String> getFirstRightGrams(final List<String> tokenList, final int minGramLength,
                                                   final int maxGramLength, final int startPosition) {
        Preconditions.checkArgument(minGramLength > 0 && maxGramLength > 0,
                "Right Gram lengths should not be negative or zero.");
        Preconditions.checkArgument(maxGramLength >= minGramLength,
                "Maximum right gram length should be at least equal to the minimum value.");
        Preconditions.checkArgument(tokenList != null);

        final int noOfTokens = tokenList.size();
        if (noOfTokens < minGramLength) {
            return Collections.emptyList();
        }

        final List<String> result = new ArrayList<>();
        final StringBuilder currentGram = new StringBuilder(tokenList.get(startPosition));
        for (int i = minGramLength; i <= maxGramLength; i++) {
            final int endPosition = startPosition + i - 1;
            if (endPosition > tokenList.size() - 1) {
                break;
            }
            currentGram.append(Constants.SPACE_SEPARATOR).append(tokenList.get(endPosition));
            result.add(currentGram.toString());
        }
        return result;
    }

    private static float adjustValenceIfLeftGramsHaveIdioms(final float currentValence,
                                                            final List<String> leftGramSequences) {
        float newValence = currentValence;
        for (String leftGramSequence : leftGramSequences) {
            if (Utils.SENTIMENT_LADEN_IDIOMS_VALENCE_DICTIONARY.containsKey(leftGramSequence)) {
                newValence = Utils.SENTIMENT_LADEN_IDIOMS_VALENCE_DICTIONARY.get(leftGramSequence);
                break;
            }
        }

        for (int i = leftGramSequences.size() - 1; i <= 2; i--) {
            if (Utils.BOOSTER_DICTIONARY.containsKey(leftGramSequences.get(i))) {
                newValence += Valence.DEFAULT_DAMPING.getValue();
                break;
            }
        }

        return newValence;
    }

    private static float adjustValenceIfIdiomsFound(final float currentValence, final int currentItemPosition,
                                                    final List<String> wordsAndEmoticons, final int distance) {
        float newValence;

        final List<String> leftGramSequences = getLeftGrams(wordsAndEmoticons, 2,
                Constants.MAX_GRAM_WINDOW_SIZE, currentItemPosition, distance);
        newValence = adjustValenceIfLeftGramsHaveIdioms(currentValence, leftGramSequences);

        final List<String> rightGramSequences = getFirstRightGrams(wordsAndEmoticons, 2,
                Constants.MAX_GRAM_WINDOW_SIZE, currentItemPosition);
        for (String rightGramSequence : rightGramSequences) {
            if (Utils.SENTIMENT_LADEN_IDIOMS_VALENCE_DICTIONARY.containsKey(rightGramSequence)) {
                newValence = Utils.SENTIMENT_LADEN_IDIOMS_VALENCE_DICTIONARY.get(rightGramSequence);
            }
        }

        return newValence;
    }

    private static List<Float> getTokenWiseSentiment(final TextProperties textProperties) {
        List<Float> sentiments = new ArrayList<>();
        final List<String> wordsAndEmoticons = textProperties.getWordsAndEmoticons();

        for (int currentItemPosition = 0; currentItemPosition < wordsAndEmoticons.size(); currentItemPosition++) {
            final String currentItem = wordsAndEmoticons.get(currentItemPosition);
            final String currentItemLower = currentItem.toLowerCase();
            float currentValence = 0.0F;

            if ((currentItemPosition < wordsAndEmoticons.size() - 1
                    && currentItemLower.equals(SentimentModifyingTokens.KIND.getValue())
                    && wordsAndEmoticons.get(currentItemPosition + 1).toLowerCase()
                    .equals(SentimentModifyingTokens.OF.getValue()))
                    || Utils.BOOSTER_DICTIONARY.containsKey(currentItemLower)) {
                sentiments.add(currentValence);
                continue;
            }

            if (Utils.WORD_VALENCE_DICTIONARY.containsKey(currentItemLower)) {
                currentValence = Utils.WORD_VALENCE_DICTIONARY.get(currentItemLower);

                if (Utils.isUpper(currentItem) && textProperties.isYelling()) {
                    if (currentValence > 0.0) {
                        currentValence += Valence.ALL_CAPS_FACTOR.getValue();
                    } else {
                        currentValence -= Valence.ALL_CAPS_FACTOR.getValue();
                    }
                }

                int distance = 0;
                while (distance < Constants.MAX_GRAM_WINDOW_SIZE) {
                    int closeTokenIndex = currentItemPosition - (distance + 1);
                    if (closeTokenIndex < 0) {
                        closeTokenIndex = wordsAndEmoticons.size() - Math.abs(closeTokenIndex);
                    }

                    if ((currentItemPosition > distance)
                            && !Utils.WORD_VALENCE_DICTIONARY.containsKey(wordsAndEmoticons.get(closeTokenIndex)
                            .toLowerCase())) {
                        float gramBasedValence = adjustValenceIfCapital(wordsAndEmoticons.get(closeTokenIndex),
                                currentValence, textProperties.isYelling());
                        if (gramBasedValence != 0.0F) {
                            if (distance == 1) {
                                gramBasedValence *= Valence.ONE_WORD_DISTANCE_DAMPING_FACTOR.getValue();
                            } else if (distance == 2) {
                                gramBasedValence *= Valence.TWO_WORD_DISTANCE_DAMPING_FACTOR.getValue();
                            }
                        }
                        currentValence += gramBasedValence;
                        currentValence = dampValenceIfNegativeTokensFound(currentValence, distance,
                                currentItemPosition, closeTokenIndex, wordsAndEmoticons);

                        if (distance == 2) {
                            currentValence = adjustValenceIfIdiomsFound(currentValence, currentItemPosition,
                                    wordsAndEmoticons, distance);
                        }
                    }

                    distance++;
                }
                currentValence = adjustValenceIfHasAtLeast(currentItemPosition, wordsAndEmoticons, currentValence);
            }

            sentiments.add(currentValence);
        }
        sentiments = adjustValenceIfHasConjunction(wordsAndEmoticons, sentiments);

        return sentiments;
    }

    private static RawSentimentScores computeRawSentimentScores(final List<Float> tokenWiseSentimentState,
                                                                final float punctuationAmplifier) {
        float positiveSentimentScore = 0.0F;
        float negativeSentimentScore = 0.0F;
        int neutralSentimentCount = 0;
        for (Float valence : tokenWiseSentimentState) {
            if (valence > 0.0F) {
                positiveSentimentScore += valence + 1.0F;
            } else if (valence < 0.0F) {
                negativeSentimentScore += valence - 1.0F;
            } else {
                neutralSentimentCount += 1;
            }
        }

        if (positiveSentimentScore > Math.abs(negativeSentimentScore)) {
            positiveSentimentScore += punctuationAmplifier;
        } else if (positiveSentimentScore < Math.abs(negativeSentimentScore)) {
            negativeSentimentScore -= punctuationAmplifier;
        }

        return new RawSentimentScores(positiveSentimentScore, negativeSentimentScore, (float) neutralSentimentCount);
    }

    private static float computeCompoundPolarityScore(final List<Float> tokenWiseSentimentState, final float punctuationAmplifier) {
        float totalValence = tokenWiseSentimentState.stream().reduce(0.0F, Float::sum);

        if (totalValence > 0.0F) {
            totalValence += punctuationAmplifier;
        } else if (totalValence < 0.0F) {
            totalValence -= punctuationAmplifier;
        }

        return totalValence;
    }

    private static SentimentPolarities normalizeAllScores(final RawSentimentScores rawSentimentScores,
                                                          final float compoundPolarityScore) {
        final float positiveSentimentScore = rawSentimentScores.getPositiveScore();
        final float negativeSentimentScore = rawSentimentScores.getNegativeScore();
        final int neutralSentimentCount = Math.round(rawSentimentScores.getNeutralScore());

        final float normalizationFactor = positiveSentimentScore + Math.abs(negativeSentimentScore)
                + neutralSentimentCount;

        final float absolutePositivePolarity = Math.abs(positiveSentimentScore / normalizationFactor);
        final float absoluteNegativePolarity = Math.abs(negativeSentimentScore / normalizationFactor);
        final float absoluteNeutralPolarity = Math.abs(neutralSentimentCount / normalizationFactor);

        final float normalizedPositivePolarity = roundDecimal(absolutePositivePolarity, 3);
        final float normalizedNegativePolarity = roundDecimal(absoluteNegativePolarity, 3);
        final float normalizedNeutralPolarity = roundDecimal(absoluteNeutralPolarity, 3);

        final float normalizedCompoundPolarity = roundDecimal(normalizeCompoundScore(compoundPolarityScore), 4);

        return new SentimentPolarities(normalizedPositivePolarity, normalizedNegativePolarity,
                normalizedNeutralPolarity, normalizedCompoundPolarity);
    }

    private static SentimentPolarities getPolarityScores(final List<Float> tokenWiseSentimentStateParam, final float punctuationAmplifier) {
        final List<Float> tokenWiseSentimentState = Collections.unmodifiableList(tokenWiseSentimentStateParam);

        final float compoundPolarity = computeCompoundPolarityScore(tokenWiseSentimentState, punctuationAmplifier);
        final RawSentimentScores rawSentimentScores = computeRawSentimentScores(tokenWiseSentimentState,
                punctuationAmplifier);

        return normalizeAllScores(rawSentimentScores, compoundPolarity);
    }

    private static float boostByPunctuation(String input) {
        return boostByExclamation(input) + boostByQuestionMark(input);
    }

    private static float boostByExclamation(String input) {
        final int exclamationCount =
                StringUtils.countMatches(input, SentimentModifyingTokens.EXCLAMATION_MARK.getValue());
        return Math.min(exclamationCount, Constants.MAX_EXCLAMATION_MARKS)
                * Valence.EXCLAMATION_BOOSTING.getValue();
    }

    private static float boostByQuestionMark(String input) {
        final int questionMarkCount =
                StringUtils.countMatches(input, SentimentModifyingTokens.QUESTION_MARK.getValue());
        float questionMarkAmplifier = 0.0F;
        if (questionMarkCount > 1) {
            if (questionMarkCount <= Constants.MAX_QUESTION_MARKS) {
                questionMarkAmplifier = questionMarkCount * Valence.QUESTION_MARK_MAX_COUNT_BOOSTING.getValue();
            } else {
                questionMarkAmplifier = Valence.QUESTION_MARK_BOOSTING.getValue();
            }
        }
        return questionMarkAmplifier;
    }

    private static List<Float> adjustValenceIfHasConjunction(final List<String> inputTokensParam,
                                                             final List<Float> tokenWiseSentimentStateParam) {
        final List<String> inputTokens = Collections.unmodifiableList(inputTokensParam);
        final List<Float> tokenWiseSentimentState = new ArrayList<>(tokenWiseSentimentStateParam);

        int indexOfConjunction = inputTokens.indexOf(SentimentModifyingTokens.BUT.getValue());
        if (indexOfConjunction < 0) {
            indexOfConjunction = inputTokens.indexOf(SentimentModifyingTokens.BUT.getValue().toUpperCase());
        }
        if (indexOfConjunction >= 0) {
            for (int valenceIndex = 0; valenceIndex < tokenWiseSentimentState.size(); valenceIndex++) {
                float currentValence = tokenWiseSentimentState.get(valenceIndex);
                if (valenceIndex < indexOfConjunction) {
                    currentValence *= Valence.PRE_CONJUNCTION_ADJUSTMENT_FACTOR.getValue();
                } else if (valenceIndex > indexOfConjunction) {
                    currentValence *= Valence.POST_CONJUNCTION_ADJUSTMENT_FACTOR.getValue();
                }
                tokenWiseSentimentState.set(valenceIndex, currentValence);
            }
        }
        return tokenWiseSentimentState;
    }

    private static float adjustValenceIfHasAtLeast(final int currentItemPosition,
                                                   final List<String> wordsAndEmoticonsParam,
                                                   final float currentValence) {
        final List<String> wordsAndEmoticons = Collections.unmodifiableList(wordsAndEmoticonsParam);
        float valence = currentValence;
        if (currentItemPosition > 1
                && !Utils.WORD_VALENCE_DICTIONARY.containsKey(wordsAndEmoticons.get(currentItemPosition - 1)
                .toLowerCase())
                && wordsAndEmoticons.get(currentItemPosition - 1)
                .toLowerCase().equals(SentimentModifyingTokens.LEAST.getValue())) {
            if (!(wordsAndEmoticons.get(currentItemPosition - 2).toLowerCase()
                    .equals(SentimentModifyingTokens.AT.getValue())
                    || wordsAndEmoticons.get(currentItemPosition - 2).toLowerCase()
                    .equals(SentimentModifyingTokens.VERY.getValue()))) {
                valence *= Valence.NEGATIVE_WORD_DAMPING_FACTOR.getValue();
            }
        } else if (currentItemPosition > 0
                && !Utils.WORD_VALENCE_DICTIONARY.containsKey(wordsAndEmoticons.get(currentItemPosition - 1).toLowerCase())
                && wordsAndEmoticons.get(currentItemPosition - 1).equals(SentimentModifyingTokens.LEAST.getValue())) {
            valence *= Valence.NEGATIVE_WORD_DAMPING_FACTOR.getValue();
        }
        return valence;
    }

    private static boolean hasContraction(final String token) {
        return token.endsWith(SentimentModifyingTokens.CONTRACTION.getValue());
    }

    private static boolean isNegative(final String token, final boolean checkContractions) {
        final boolean result = Utils.NEGATIVE_WORDS.contains(token);
        if (!checkContractions) {
            return result;
        }
        return result || hasContraction(token);
    }

    private static boolean isNegative(final String token) {
        return isNegative(token, true);
    }

    private static float normalizeCompoundScore(final float score, final float alpha) {
        final double normalizedScore = score / Math.sqrt((score * score) + alpha);
        return (float) normalizedScore;
    }

    private static float normalizeCompoundScore(final float score) {
        return normalizeCompoundScore(score, Constants.DEFAULT_ALPHA);
    }

    private static float roundDecimal(final float currentValue, final int noOfPlaces) {
        final float factor = (float) Math.pow(10.0, (double) noOfPlaces);
        final float number = Math.round(currentValue * factor);
        return number / factor;
    }

    private static SentimentPolarities computeSentimentPolaritiesFor(String inputString) {
        final TextProperties inputStringProperties;
        try {
            inputStringProperties = new TextProperties(inputString);
        } catch (IOException excp) {
            return SentimentPolarities.emptySentimentState();
        }

        final List<Float> tokenWiseSentiments = getTokenWiseSentiment(inputStringProperties);
        if (tokenWiseSentiments.isEmpty()) {
            return SentimentPolarities.emptySentimentState();
        }
        final float punctuationAmplifier = boostByPunctuation(inputString);
        return getPolarityScores(tokenWiseSentiments, punctuationAmplifier);
    }
}