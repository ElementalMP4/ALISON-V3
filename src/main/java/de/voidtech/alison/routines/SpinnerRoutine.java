package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.annotations.Routine;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.service.SpinnerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
                    .setDescription("<@%s>'s spinner was knocked over by <@%s>! It lasted for %s".formatted(spinner.getUserID(), spinner.getKnockedOverBy(), secondsToTime(spinner.getSpinnerDurationSeconds())))
                    .build();
            message.getChannel().sendMessageEmbeds(embed).queue();
        }
    }

    private String secondsToTime(long duration) {
        long days = duration / (24 * 3600);
        duration = duration % (24 * 3600);

        long hours = duration / 3600;
        duration %= 3600;

        long minutes = duration / 60;
        duration %= 60;

        long seconds = duration;
        List<String> output = new ArrayList<>();

        if (days > 0) output.add(days + " days");
        if (hours > 0) output.add(hours + " hours");
        if (minutes > 0) output.add(minutes + " minutes");
        output.add(seconds + " seconds");
        return String.join(", ", output);
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