package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.listeners.MessageListener;
import main.java.de.voidtech.alison.routines.AbstractRoutine;
import main.java.de.voidtech.alison.util.LevenshteinCalculator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MessageService {

    public static final Logger LOGGER = Logger.getLogger(MessageListener.class.getSimpleName());
    private static final int LEVENSHTEIN_THRESHOLD = 3;

    @Autowired
    private List<AbstractCommand> commands;

    @Autowired
    private List<AbstractRoutine> routines;

    @Autowired
    private ConfigService config;

    @Autowired
    private PrivacyService privacyService;

    private boolean shouldHandleAsChatCommand(String prefix, Message message) {
        String messageRaw = message.getContentRaw();
        return messageRaw.startsWith(prefix) && messageRaw.length() > prefix.length();
    }

    public void handleMessage(Message message) {
        if (message.getAuthor().getId().equals(message.getJDA().getSelfUser().getId())) return;
        String prefix = config.getDefaultPrefix();

        boolean messageIsCommand = shouldHandleAsChatCommand(prefix, message);

        performNonCommandMessageActions(message, messageIsCommand);
        if (!messageIsCommand) {
            return;
        }

        if (message.getChannel().getType().equals(ChannelType.PRIVATE)) doTheCommanding(message, prefix);
        else if (!privacyService.channelIsIgnored(message.getChannel().getId(), message.getGuild().getId())) doTheCommanding(message, prefix);
        else if (message.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) doTheCommanding(message, prefix);
    }


    private void performNonCommandMessageActions(Message message, boolean messageIsCommand) {
        if (privacyService.channelIsIgnored(message))
            return;
        if (message.getContentRaw().equals(""))
            return;
        if (privacyService.userHasOptedOut(message.getAuthor().getId()))
            return;
        runMessageRoutines(message, messageIsCommand);
    }

    private void runMessageRoutines(Message message, boolean messageIsCommand) {
        for (AbstractRoutine routine : routines) {
            if (message.getChannel().getType().equals(ChannelType.PRIVATE)) {
                if (routine.isDmCapable()) routine.run(message, messageIsCommand);
            } else routine.run(message, messageIsCommand);
        }
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
        } else {
            commandOpt.run(new CommandContext(message, messageArray.subList(1, messageArray.size())));
        }
    }

    private MessageEmbed createLevenshteinEmbed(List<String> possibleOptions) {
        EmbedBuilder levenshteinResultEmbed = new EmbedBuilder().setColor(Color.RED).setTitle(
                "I couldn't find that command! Did you mean `" + String.join("`, `", possibleOptions) + "`?");
        return levenshteinResultEmbed.build();
    }

    private void tryLevenshteinOptions(Message message, String commandName) {
        List<String> possibleOptions;
        possibleOptions = commands.stream()
                .map(AbstractCommand::getName)
                .filter(name -> LevenshteinCalculator.calculate(commandName, name) <= LEVENSHTEIN_THRESHOLD)
                .collect(Collectors.toList());
        if (!possibleOptions.isEmpty())
            message.replyEmbeds(createLevenshteinEmbed(possibleOptions)).mentionRepliedUser(false).queue();
    }
}