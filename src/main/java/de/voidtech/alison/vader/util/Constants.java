package main.java.de.voidtech.alison.vader.util;

import java.util.regex.Pattern;

public final class Constants {

    public static final int MAX_QUESTION_MARKS = 3;
    public static final int PRECEDING_TRIGRAM_WINDOW = 3;
    public static final int PRECEDING_BIGRAM_WINDOW = 2;
    public static final int PRECEDING_UNIGRAM_WINDOW = 1;
    public static final int MAX_EXCLAMATION_MARKS = 4;
    public static final int MAX_GRAM_WINDOW_SIZE = 3;
    public static final float DEFAULT_ALPHA = 15.0F;
    public static final Pattern NON_NUMERIC_STRING_REGEX = Pattern.compile(".*[a-zA-Z]+.*");
    public static final String HTTP_URL_PREFIX = "http://";
    public static final String HTTPS_URL_PREFIX = "https://";
    public static final String SPACE_SEPARATOR = " ";
}
