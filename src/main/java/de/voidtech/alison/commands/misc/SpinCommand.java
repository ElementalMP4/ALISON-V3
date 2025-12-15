package main.java.de.voidtech.alison.commands.misc;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.service.SpinnerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class SpinCommand extends AbstractCommand {

    @Autowired
    private SpinnerService spinnerService;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (args.isEmpty()) {
            spinnerService.createSpinner(commandContext.getMessage());
            MessageEmbed spinEmbed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setDescription("<@%s>'s spinner is spinning...".formatted(commandContext.getAuthor().getId()))
                    .build();
            commandContext.reply(spinEmbed);
        } else {
            switch (args.get(0).toLowerCase()) {
                case "leaderboard", "lb" -> showLeaderboard(commandContext);
                default -> commandContext.reply("Unknown command");
            }
        }
    }

    private void showLeaderboard(CommandContext commandContext) {
        List<Spinner> leaderboard = spinnerService.getServerLeaderboard(commandContext.getGuild().getId());
        StringBuilder leaderboardBuilder = new StringBuilder();

        int pos = 1;
        for (Spinner spinner : leaderboard) {
            leaderboardBuilder.append("**%d - <@%s> in <#%s>**\n".formatted(pos, spinner.getUserID(), spinner.getChannelID()));
            leaderboardBuilder.append("```\n");
            leaderboardBuilder.append("Lasted: %s\n".formatted(spinner.durationAsText()));
            leaderboardBuilder.append("```\n\n");
        }

        MessageEmbed leaderboardEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("%s Leaderboard".formatted(commandContext.getGuild().getName()))
                .setDescription(leaderboardBuilder.toString())
                .build();

        commandContext.reply(leaderboardEmbed);
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
        return "Allows you to start a spinner in any channel. If someone messages in that channel, " +
                "the spinner will be knocked over and the time your spinner was spinning for will be added to a leaderboard.";
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
}