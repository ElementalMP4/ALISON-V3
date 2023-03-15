package main.java.de.voidtech.alison.commands.misc;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.SpinnerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Command
public class SpinCommand extends AbstractCommand {

    private static final String CROSS_UNICODE = "U+274c";

    @Autowired
    private SpinnerService spinnerService;

    @Autowired
    private EventWaiter waiter;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (args.isEmpty()) {
            spinnerService.createSpinner(commandContext.getMessage());
            new SpinnerReactionListener(commandContext, waiter);
        } else {
            //TODO
        }
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
                "the spinner will be knocked over and the time your spinner was spinning for will be added to a leaderboard.\n" +
                "Hint: If you want to be extra sneaky, react to the bot embed message with an ❌ to delete it!";
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

    private class SpinnerReactionListener {
        public SpinnerReactionListener(CommandContext context, EventWaiter waiter) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle(String.format("<@%s>'s spinner is spinning...", context.getAuthor().getId()))
                    .setDescription("If anyone sends a message here, it'll get knocked over!")
                    .build();
            Message msg = context.getMessage().getChannel().sendMessageEmbeds(embed).complete();
            waiter.waitForEvent(MessageReactionAddEvent.class,
                    e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getMessageId().equals(msg.getId()),
                    e -> {if (e.getReactionEmote().toString().equals("RE:" + CROSS_UNICODE)) msg.delete().queue();}, 30, TimeUnit.SECONDS,
                    () -> msg.editMessageComponents().queue());
        }
    }
}
