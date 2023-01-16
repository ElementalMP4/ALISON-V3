package main.java.de.voidtech.alison.service;

import org.springframework.stereotype.Service;

@Service
public class ClaireService {

    public String createReply(String prompt) {
        return createReply(prompt, TextGenerationService.CLAIRE_LENGTH);
    }

    public String createReply(String promptForAlison, int promptLength) {
        return null;
    }

    public long getConversationCount() {
        return 0;
    }
}
