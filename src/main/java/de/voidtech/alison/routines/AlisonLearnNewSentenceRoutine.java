package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.service.AlisonService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

@Routine
public class AlisonLearnNewSentenceRoutine extends AbstractRoutine {

    @Autowired
    private AlisonService textGenerationService;

    @Override
    public void executeInternal(Message message) {
        textGenerationService.learn(message.getAuthor().getId(), message.getContentRaw());
    }

    @Override
    public String getName() {
        return "alison-learn-new-sentence";
    }

    @Override
    public String getDescription() {
        return "Allows ALISON to learn new sentences and add them to the ALISON repository";
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
