package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.vader.analyser.SentimentPolarities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Command
public class HowToxicAmICommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private AnalysisService analyser;

	@Override
	public void execute(CommandContext context) {
		User user;

		if (context.isSlashCommand()) {
			if (context.getEvent().getOption("user") == null) user = context.getAuthor();
			else user = context.getEvent().getOption("user").getAsUser();
		} else {
			if (context.getArgs().isEmpty()) user = context.getAuthor();
			else {
				String id = context.getArgs().get(0).replaceAll("([^0-9a-zA-Z])", "");
				Result<User> userResult = context.getJDA().retrieveUserById(id).mapToResult().complete();
				if (!userResult.isSuccess()) {
					context.reply("I couldn't find that user!");
					return;
				}
				user = userResult.get();
			}
    	}
		analyse(user, context);
	}
	
	private void analyse(User user, CommandContext context) {
		if (privacyService.userHasOptedOut(user.getId())) {
			context.reply("This user has chosen not to be analysed!");
			return;
		}
		SentimentPolarities sentiment = analyser.analyseCollection(user.getId());
		if (sentiment == null) {
			context.reply("I couldn't find any data to analyse!");
			return;
		}
		MessageEmbed toxicityEmbed = new EmbedBuilder()
				.setColor(getColour(sentiment))
				.setTitle("How toxic is " + user.getName() + "?")
				.addField(":grin: Positive Score", "```\n" + sentiment.getPositivePolarity() + "\n```", true)
				.addField(":neutral_face: Neutral Score", "```\n" + sentiment.getNeutralPolarity() + "\n```", true)
				.addField(":angry: Negative Score", "```\n" + sentiment.getNegativePolarity() + "\n```", true)
				.addField("Compound Score", "```\n" + sentiment.getCompoundPolarity() + "\n```", false)
				.setFooter(getMessage(sentiment))
				.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? "You are known by the state of california to cause crippling sadness"
				: howToxic.getCompoundPolarity() > 0 ? "You are positively epic"
				: "You are completely mid";
	}

	private Color getColour(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? Color.RED
				: howToxic.getCompoundPolarity() > 0 ? Color.GREEN
				: Color.ORANGE;
	}

	@Override
	public String getName() {
		return "howtoxic";
	}

	@Override
	public String getUsage() {
		return "howtoxic";
	}

	@Override
	public String getDescription() {
		return "See how toxic you are";
	}

	@Override
	public String getShorthand() {
		return "ht";
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
		return CommandCategory.SENTIMENT_ANALYSIS;
	}

	@Override
	public SlashCommandOptions getSlashCommandOptions() {
		return new SlashCommandOptions(new OptionData(OptionType.USER, "user", "The user to judge", false));
	}
}