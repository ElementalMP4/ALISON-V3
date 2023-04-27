package main.java.de.voidtech.alison.vader.processor;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Consumer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class InputAnalyser implements InputAnalyserInterface {

    protected void tokenize(final String inputString, final Tokenizer tokenizer, final Consumer<String> tokenConsumer) throws IOException {
        tokenizer.setReader(new StringReader(inputString));

        try (TokenStream tokenStream = new LengthFilter(tokenizer, 2, Integer.MAX_VALUE)) {
            final CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                tokenConsumer.accept(charTermAttribute.toString());
            }

            tokenStream.end();
        }
    }

    @Override
    public void keepPunctuation(final String inputString, final Consumer<String> tokenConsumer) throws IOException {
        tokenize(inputString, new WhitespaceTokenizer(), tokenConsumer);
    }

    @Override
    public void removePunctuation(final String inputString, final Consumer<String> tokenConsumer) throws IOException {
        tokenize(inputString, new StandardTokenizer(), tokenConsumer);
    }
}
