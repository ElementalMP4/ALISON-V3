package main.java.de.voidtech.alison.vader.processor;

import java.io.IOException;
import java.util.function.Consumer;

public interface InputAnalyserInterface {
    void keepPunctuation(String inputString, Consumer<String> tokenConsumer) throws IOException;
    void removePunctuation(String inputString, Consumer<String> tokenConsumer) throws IOException;
}