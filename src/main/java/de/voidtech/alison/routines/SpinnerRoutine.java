package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.service.SpinnerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Routine
public class SpinnerRoutine extends AbstractRoutine {

    @Autowired
    private SpinnerService spinnerService;

    @Override
    public void executeInternal(Message message) {
        if (spinnerService.spinnerExistsInChannel(message.getChannel().getId())) {
            Spinner spinner = spinnerService.endSpinnage(message);
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Spinner knocked over!")
                    .setDescription("<@%s>'s spinner was knocked over by <@%s>! It lasted for %s".formatted(spinner.getUserID(), spinner.getKnockedOverBy(), spinner.durationAsText()))
                    .build();
            message.getChannel().sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public String getName() {
        return "spinner-detector";
    }

    @Override
    public String getDescription() {
        return "Detects when spinners are knocked over in a channel (started by the spin command)";
    }

    @Override
    public boolean isDmCapable() {
        return false;
    }

    @Override
    public boolean ignoreCommands() {
        return false;
    }
}