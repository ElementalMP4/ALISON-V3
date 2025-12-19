package main.java.de.voidtech.alison.commands.misc;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.SpinnerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Command
public class SpinCommand extends AbstractCommand {

    @Autowired
    private SpinnerService spinnerService;

    @Override
    protected void execute(CommandContext commandContext) {
        spinnerService.createSpinner(commandContext.getMessage());
        MessageEmbed spinEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setDescription("<@%s>'s spinner is spinning...".formatted(commandContext.getAuthor().getId()))
                .build();
        commandContext.reply(spinEmbed);
    }

    @Override
    public String getName() {
        return "spin";
    }

    @Override
    public String getUsage() {
        return "spin\n" +
                "spin leaderboard";
    }

    @Override
    public String getDescription() {
        return "Start spinning a spinner";
    }

    @Override
    public String getShorthand() {
        return "spin";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MISC;
    }

    @Override
    public boolean isDmCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public SlashCommandOptions getSlashCommandOptions() {
        return new SlashCommandOptions();
    }
}