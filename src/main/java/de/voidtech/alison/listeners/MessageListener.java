package main.java.de.voidtech.alison.listeners;

import main.java.de.voidtech.alison.Alison;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.*;
import main.java.de.voidtech.alison.util.LevenshteinCalculator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class MessageListener implements EventListener {

    @Autowired
    private List<AbstractCommand> commands;

    @Autowired
    private ConfigService config;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private TextGenerationService textGenerationService;

    @Autowired
    private ClaireService claireService;

    @Autowired
    private AnalysisService analysisService;

    public static final Logger LOGGER = Logger.getLogger(MessageListener.class.getSimpleName());
    private static final int LEVENSHTEIN_THRESHOLD = 3;

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent message = (MessageReceivedEvent) event;
            if (message.isWebhookMessage())
                return;
            handleMessage(message.getMessage());
        }
    }

    private boolean shouldHandleAsChatCommand(String prefix, Message message) {
        String messageRaw = message.getContentRaw();
        return messageRaw.startsWith(prefix) && messageRaw.length() > prefix.length();
    }

    private void handleMessage(Message message) {
        if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) return;
        String prefix = config.getDefaultPrefix();
        if (!shouldHandleAsChatCommand(prefix, message)) {
            performNonCommandMessageActions(message);
            return;
        }

        if (message.getChannel().getType().equals(ChannelType.PRIVATE)) doTheCommanding(message, prefix);
        else if (!privacyService.channelIsIgnored(message.getChannel().getId(), message.getGuild().getId())) doTheCommanding(message, prefix);
        else if (message.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) doTheCommanding(message, prefix);
    }


    private void performNonCommandMessageActions(Message message) {
        //if (message.getChannel().getType().equals(ChannelType.PRIVATE)) {
            //ReplyManager.replyToMessage(message);
            //return;
        //}
        if (privacyService.channelIsIgnored(message.getChannel().getId(), message.getGuild().getId()))
            return;
        if (message.getContentRaw().equals(""))
            return;
        if (privacyService.userHasOptedOut(message.getAuthor().getId()))
            return;
        claireService.addMessages(message);
        analysisService.respondToAlisonMention(message);
        textGenerationService.learn(message.getAuthor().getId(), message.getContentRaw());
        claireService.replyToMessage(message);
    }

    private void doTheCommanding(Message message, String prefix) {
        String messageContent = message.getContentRaw().substring(prefix.length());
        List<String> messageArray = Arrays.asList(messageContent.trim().split("\\s+"));

        String commandToFind = messageArray.get(0).toLowerCase();
        AbstractCommand commandOpt = commands.stream()
                .filter(c -> c.getName().equals(commandToFind) || c.getShorthand().equals(commandToFind))
                .findFirst()
                .orElse(null);

        if (commandOpt == null) {
            LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
            tryLevenshteinOptions(message, messageArray.get(0));
        } else commandOpt.run(new CommandContext(message), messageArray.subList(1, messageArray.size()));
    }

    private MessageEmbed createLevenshteinEmbed(List<String> possibleOptions) {
        EmbedBuilder levenshteinResultEmbed = new EmbedBuilder().setColor(Color.RED).setTitle(
                "I couldn't find that command! Did you mean `" + String.join("` or `", possibleOptions) + "`?");
        return levenshteinResultEmbed.build();
    }

    private void tryLevenshteinOptions(Message message, String commandName) {
        List<String> possibleOptions;
        possibleOptions = commands.stream()
                .map(AbstractCommand::getName)
                .filter(name -> LevenshteinCalculator.calculate(commandName, name) <= LEVENSHTEIN_THRESHOLD)
                .collect(Collectors.toList());
        if (!possibleOptions.isEmpty())
            message.getChannel().sendMessageEmbeds(createLevenshteinEmbed(possibleOptions)).queue();
    }
}