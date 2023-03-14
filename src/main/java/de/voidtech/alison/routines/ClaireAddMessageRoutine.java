package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.service.ClaireService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

@Routine
public class ClaireAddMessageRoutine extends AbstractRoutine {

    @Autowired
    private ClaireService claireService;

    @Override
    public void executeInternal(Message message) {
        claireService.addMessages(message);
    }

    @Override
    public String getName() {
        return "claire-add-messages";
    }

    @Override
    public String getDescription() {
        return "Allows ALISON to add message pairs to the CLAIRE repository";
    }
}
