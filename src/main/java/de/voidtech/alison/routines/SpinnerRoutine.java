package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.entities.Spinner;
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
                    .setColor(Color.ORANGE)
                    .setTitle(String.format("<@%s>'s spinner was knocked over by <@%s>!", spinner.getUserID(), spinner.getKnockedOverBy()))
                    .setDescription(String.format("It lasted for <t:%d:T>", spinner.getSpinnerDurationSeconds()))
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
}
