package main.java.de.voidtech.alison.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static main.java.de.voidtech.alison.util.ResourceLoader.resourceAsString;

@Service
public class ReallyCommonEnglishService {

    @Value("classpath:ReallyCommonEnglish.txt")
    private Resource reallyCommonEnglishResource;

    //The most commonly used words in the english language. Source? Trust me bro
    private static List<String> ReallyCommonEnglish;

    @EventListener(ApplicationReadyEvent.class)
    void loadReallyCommonEnglish() {
        String commonEnglish = resourceAsString(reallyCommonEnglishResource);
        ReallyCommonEnglish = Arrays.asList(commonEnglish.split("\n"));
    }

    public List<String> filterOutPointlessContext(List<String> words) {
        List<String> filteredWords = words.stream()
                .filter(w -> !ReallyCommonEnglish.contains(w.toLowerCase().replaceAll("[^a-z A-Z]", "")))
                .collect(Collectors.toList());
        return filteredWords.isEmpty() ? words : filteredWords;
    }
}
