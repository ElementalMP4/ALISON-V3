package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.service.AnalysisService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

@Routine
public class AlisonMentionResponseRoutine extends AbstractRoutine {

    @Autowired
    private AnalysisService analysisService;

    @Override
    public void executeInternal(Message message) {
        analysisService.respondToAlisonMention(message);
    }

    @Override
    public String getName() {
        return "alison-mention-response";
    }

    @Override
    public String getDescription() {
        return "Allows ALISON to react to the sentiment of a message that contains 'alison' ";
    }

    @Override
    public boolean isDmCapable() {
        return true;
    }

    @Override
    public boolean ignoreCommands() {
        return true;
    }
}
