package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.vader.analyser.SentimentPolarities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class HowToxicIsThis extends AbstractCommand {

    @Autowired
    private AnalysisService analysisService;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (!args.isEmpty()) {
            String message = String.join(" ", args);
            analyse(commandContext, message);
        } else {
            if (commandContext.getMessage().getReferencedMessage() == null) {
                commandContext.reply("You need to either supply your own message or reply to someone else's with this command!");
                return;
            }
            String message = commandContext.getMessage().getReferencedMessage().getContentRaw();
            analyse(commandContext, message);
        }
    }

    private void analyse(CommandContext context, String message) {
        SentimentPolarities sentiment = analysisService.analyseSentence(message);
        MessageEmbed toxicityEmbed = new EmbedBuilder()
                .setColor(getColour(sentiment))
                .setTitle("Sentiment Report")
                .addField(":grin: Positive Score", "```\n" + sentiment.getPositivePolarity() + "\n```", false)
                .addField(":neutral_face: Neutral Score", "```\n" + sentiment.getNeutralPolarity() + "\n```", false)
                .addField(":angry: Negative Score", "```\n" + sentiment.getNegativePolarity() + "\n```", false)
                .addField("Compound Score", "```\n" + sentiment.getCompoundPolarity() + "\n```", false)
                .setFooter(getMessage(sentiment))
                .build();
        context.reply(toxicityEmbed);
    }

    private String getMessage(SentimentPolarities howToxic) {
        return howToxic.getCompoundPolarity() < 0 ? "This is one of the worst things I have ever read"
                : howToxic.getCompoundPolarity() > 0 ? "This is a very lovely thing to say"
                : "This message is entirely neutral";
    }

    private Color getColour(SentimentPolarities howToxic) {
        return howToxic.getCompoundPolarity() < 0 ? Color.RED
                : howToxic.getCompoundPolarity() > 0 ? Color.GREEN
                : Color.ORANGE;
    }

    @Override
    public String getName() {
        return "howtoxicisthis";
    }

    @Override
    public String getUsage() {
        return "howtoxicisthis [message]\n" +
                "howtoxicisthis (reply to a message with this command)";
    }

    @Override
    public String getDescription() {
        return "See how toxic a string of text or a message is (by replying to the message with this command)";
    }

    @Override
    public String getShorthand() {
        return "htthis";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.SENTIMENT_ANALYSIS;
    }

    @Override
    public boolean isDmCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }
}
