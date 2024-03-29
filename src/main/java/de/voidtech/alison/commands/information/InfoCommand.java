package main.java.de.voidtech.alison.commands.information;

import main.java.de.voidtech.alison.GlobalConstants;
import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.ClaireService;
import main.java.de.voidtech.alison.service.AlisonService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class InfoCommand extends AbstractCommand {

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private ClaireService claireService;

	@Override
	public void execute(CommandContext context, List<String> args) {
		long guildCount = context.getJDA().getGuildCache().size();
		long memberCount = context.getJDA().getGuildCache().stream().mapToInt(Guild::getMemberCount).sum();
		long wordCount = textGenerationService.getWordCount();
		long modelCount = textGenerationService.getModelCount();
		long convoCount = claireService.getConversationCount();

		MessageEmbed informationEmbed = new EmbedBuilder()
			.setColor(Color.ORANGE)
			.setTitle("ALISON - Automatic Learning Intelligent Sentence Organising Network", GlobalConstants.REPO_URL)
			.addField("Guild Count", "```" + guildCount + "```", true)
			.addField("Member Count", "```" + memberCount + "```", true)
			.addField("Active Threads", "```" + Thread.activeCount() + "```", true)
			.addField("Total Word Count", "```" + wordCount + "```", true)
				.addField("Learned Conversations", "```" + convoCount + "```", true)
			.addField("Model Count", "```" + modelCount + "```", true)
			.setDescription("**Important Privacy Notice**\n"
				+ "Data collected by ALISON is only available whilst you are opted in to the data collection program."
				+ " To stop data collection, use the optout command."
				+ " Once you are opted out, you can still use ALISON, but messages you send will not be processed or persisted.")
			.setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
			.setFooter("Use the help command to see what else I can do!\nVersion: " + GlobalConstants.VERSION,
				context.getJDA().getSelfUser().getAvatarUrl())
			.build();
		context.reply(informationEmbed);
	}

	@Override
	public String getDescription() {
		return "Provides interetsing information about ALISON including word counts and model counts";
	}

	@Override
	public String getUsage() {
		return "info";
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getShorthand() {
		return "stats";
	}

	@Override
	public boolean isDmCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFORMATION;
	}

}