package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.entities.Sentiment;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.service.PrivacyService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class HowToxicAmICommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private AnalysisService analyser;

	@Override
	public void execute(CommandContext context, List<String> args) {
		if (args.isEmpty()) analyse(context.getAuthor(), context);
    	else {
    		String userID = args.get(0).replaceAll("([^0-9])", "");
    		if (userID.equals("")) {
                context.reply("I couldn't find that user :(");
                return;
            }
            
            Result<User> userResult = context.getJDA().retrieveUserById(userID).mapToResult().complete();
            if (userResult.isSuccess()) analyse(userResult.get(), context);
            else context.reply("I couldn't find that user :(");
    	}
	}
	
	private void analyse(User user, CommandContext context) {
		if (privacyService.userHasOptedOut(user.getId())) {
			context.reply("This user has chosen not to be analysed!");
			return;
		}
		Sentiment howToxic = analyser.analyseCollection(user.getId());
		if (howToxic == null) {
			context.reply("I couldn't find any data to analyse!");
			return;
		}
		MessageEmbed toxicityEmbed = new EmbedBuilder()
				.setColor(getColour(howToxic))
				.setTitle("How toxic is " + user.getName() + "?")
				.setDescription("I searched `" + howToxic.getTotalWordCount() + "` words. From this, I found `" + howToxic.getTokenCount() + "` words with meaning.")
				.addField("Positive words found", "```\n" + howToxic.getPositiveCount() + "\n```", true)
				.addField("Negative words found",  "```\n" + howToxic.getNegativeCount() + "\n```", true)
				.addField("Total Score (higher is better!)",  "```\n" + howToxic.getScore() + "\n```", true)
				.addField("Average Score (higher is better!)",  "```\n" + howToxic.getAverageScore() + "\n```", true)
				.setFooter(getMessage(howToxic))
				.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(Sentiment howToxic) {
		return howToxic.getScore() < -2 ? "You are known by the state of california to cause crippling sadness"
				: howToxic.getScore() < 2 ? "You are positively unobjectionable"
				: "You are positively epic";
	}

	private Color getColour(Sentiment howToxic) {
		return howToxic.getScore() < -2 ? Color.RED
				: howToxic.getScore() < 2 ? Color.ORANGE
				: Color.GREEN;
	}

	@Override
	public String getName() {
		return "howtoxicami";
	}

	@Override
	public String getUsage() {
		return "howtoxicami";
	}

	@Override
	public String getDescription() {
		return "Take these numbers with a grain of salt...";
	}

	@Override
	public String getShorthand() {
		return "htme";
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
}