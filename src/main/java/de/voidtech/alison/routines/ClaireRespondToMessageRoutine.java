package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.service.ClaireService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

@Routine
public class ClaireRespondToMessageRoutine extends AbstractRoutine {

    @Autowired
    private ClaireService claireService;

    @Override
    public void executeInternal(Message message) {
        if (message.getMentions().getUsers().contains(message.getJDA().getSelfUser())
                || message.getChannel().getType().equals(ChannelType.PRIVATE)) {
            message.reply(claireService.createReply(message.getContentDisplay())).mentionRepliedUser(false).queue();
        }
    }

    @Override
    public String getName() {
        return "claire-respond-to-message";
    }

    @Override
    public String getDescription() {
        return "Allows direct messages or messages that mention ALISON to be responded to by CLAIRE";
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
