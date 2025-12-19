package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.PrivacyService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Command
public class IgnoredChannelsCommand extends AbstractCommand {

    @Autowired
    private PrivacyService privacyService;

    @Override
    public void execute(CommandContext context) {
        if (!context.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            context.reply("You need the `Manage Server` permission to use this command!");
            return;
        }

        if (context.isSlashCommand()) {
            switch (context.getEvent().getSubcommandName()) {
                case "list" -> showIgnoredChannels(context);
                case "add" -> handleAdd(context);
                case "remove" -> handleRemove(context);
            }

            return;
        }

        String command = !context.getArgs().isEmpty() ? context.getArgs().get(0) : "list";
        switch (command) {
            case "list" -> showIgnoredChannels(context);
            case "add" -> handleAdd(context);
            case "remove" -> handleRemove(context);
            default -> context.reply("You need to specify one of these subcommands:\n" + this.getUsage());
        }
    }

    private void handleAdd(CommandContext context) {
        String channelId;
        if (context.isSlashCommand()) {
            channelId = context.getEvent()
                    .getOption("channel")
                    .getAsChannel()
                    .getId();
        } else {
            if (context.getMessage().getMentions().getChannels().isEmpty()) {
                context.reply("You need to mention a channel to add to the blacklist!");
                return;
            }
            channelId = context.getMessage().getMentions().getChannels().get(0).getId();
        }

        if (privacyService.channelIsIgnored(channelId, context.getGuild().getId())) {
            context.reply("Channel <#" + channelId + "> is already blacklisted!");
            return;
        }

        privacyService.ignoreChannel(channelId, context.getGuild().getId());
        context.reply("Channel <#" + channelId + "> has been blacklisted!");
    }

    private void handleRemove(CommandContext context) {
        String channelId;
        if (context.isSlashCommand()) {
            channelId = context.getEvent()
                    .getOption("channel")
                    .getAsChannel()
                    .getId();
        } else {
            if (context.getMessage().getMentions().getChannels().isEmpty()) {
                context.reply("You need to mention a channel to remove from the blacklist!");
                return;
            }
            channelId = context.getMessage().getMentions().getChannels().get(0).getId();
        }

        if (!privacyService.channelIsIgnored(channelId, context.getGuild().getId())) {
            context.reply("Channel <#" + channelId + "> is not blacklisted!");
            return;
        }

        privacyService.unignoreChannel(channelId, context.getGuild().getId());
        context.reply("Channel <#" + channelId + "> has been whitelisted!");
    }

    private void showIgnoredChannels(CommandContext context) {
        List<String> channels = privacyService.getIgnoredChannelsForServer(context.getGuild().getId());
        String channelList = channels.isEmpty()
                ? "No channels blacklisted!"
                : channels.stream().map(c -> "<#" + c + ">").collect(Collectors.joining("\n"));

        MessageEmbed blacklistEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Blacklisted channels in " + context.getGuild().getName())
                .setThumbnail(context.getGuild().getIconUrl())
                .setDescription(channelList)
                .build();
        context.reply(blacklistEmbed);
    }

    @Override
    public String getName() {
        return "ignoredchannels";
    }

    @Override
    public String getUsage() {
        return "ignoredchannels add [channel mention]\n"
                + "ignoredchannels remove [channel mention]\n"
                + "ignoredchannels list";
    }

    @Override
    public String getDescription() {
        return "Specify channels for ALISON to ignore";
    }

    @Override
    public String getShorthand() {
        return "ic";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.PRIVACY;
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
        return new SlashCommandOptions(
                new SubcommandData("list", "List ignored channels"),
                new SubcommandData("add", "Add channel to ignore list")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to blacklist", true),
                new SubcommandData("remove", "Remove channel from ignore list")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to un-blacklist", true)
        );
    }
}
