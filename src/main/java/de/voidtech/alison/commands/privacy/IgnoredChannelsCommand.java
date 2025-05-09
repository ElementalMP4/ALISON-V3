package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.PrivacyService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;


@Command
public class IgnoredChannelsCommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Override
	public void execute(CommandContext context, List<String> args) {
		
		if (!context.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			context.reply("You need the `Manage Server` permission to use this command!");
			return;
		}
		
		String command = !args.isEmpty() ? args.get(0) : "list";		
		
		switch (command) {
		case "list":
			showIgnoredChannels(context);
			break;
		case "add":
			addToBlacklist(context);
			break;
		case "remove":
			removeFromBlacklist(context);
			break;
		default:
			context.reply("You need to specify one of these subcommands:\n" + this.getUsage());
				
		}
	}

	private void removeFromBlacklist(CommandContext context) {
		if (context.getMessage().getMentions().getChannels().isEmpty()) {
			context.reply("You need to mention a channel to remove from the blacklist!");
			return;
		}
		String channelID = context.getMessage().getMentions().getChannels().get(0).getId();
		if (privacyService.channelIsIgnored(channelID, context.getGuild().getId())) {
			privacyService.unignoreChannel(channelID, context.getGuild().getId());
			context.reply("Channel <#" + channelID + "> has been whitelisted!");
		} else {
			context.reply("Channel <#" + channelID + "> is not blacklisted!");
		}
	}

	private void addToBlacklist(CommandContext context) {
		if (context.getMessage().getMentions().getChannels().isEmpty()) {
			context.reply("You need to mention a channel to add to the blacklist!");
			return;
		}
		String channelID = context.getMessage().getMentions().getChannels().get(0).getId();
		if (privacyService.channelIsIgnored(channelID, context.getGuild().getId())) {
			context.reply("Channel <#" + channelID + "> is already blacklisted!");
		} else {
			privacyService.ignoreChannel(channelID, context.getGuild().getId());
			context.reply("Channel <#" + channelID + "> has been blacklisted!");
		}
	}

	private void showIgnoredChannels(CommandContext context) {
		List<String> channels = privacyService.getIgnoredChannelsForServer(context.getGuild().getId());
		String channelList = channels.isEmpty() ? "No channels blacklisted!" :
			channels.stream().map(c -> "<#" + c + ">").collect(Collectors.joining("\n"));
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
				+ "ignoredchannels remove all\n"
				+ "ignoredchannels list";
	}

	@Override
	public String getDescription() {
		return "Allows a list of channels to be created which will be ignored by Alison."
				+ " Alison will not respond to commands and she will not learn from any messages sent in these channels.";
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

}