package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.entities.Sentiment;
import main.java.de.voidtech.alison.service.AnalysisService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class HowToxicIsThisServerCommand extends AbstractCommand {

	@Autowired
	private AnalysisService analyser;

	@Override
	public void execute(CommandContext context, List<String> args) {
		List<Sentiment> everyMemberJudgedIntensely = analyser.analyseServer(context.getGuild());
        Sentiment topMember = everyMemberJudgedIntensely.get(0);
		Sentiment bottomMember = everyMemberJudgedIntensely.get(everyMemberJudgedIntensely.size() - 1);
		Sentiment howToxic = analyser.averageSentiment(everyMemberJudgedIntensely);
		MessageEmbed toxicityEmbed = new EmbedBuilder()
			.setColor(getColour(howToxic))
			.setTitle("How toxic is " + context.getGuild().getName() + "?")
			.setDescription("I judged `" + everyMemberJudgedIntensely.size() + "/" + context.getGuild().getMemberCount() +
					"` members and scanned `" + howToxic.getTotalWordCount() +
					"` words. From this, I found `" + howToxic.getTokenCount() + "` words with meaning.")
			.addField("Positive words found", "```\n" + howToxic.getPositiveCount() + "\n```", true)
			.addField("Negative words found",  "```\n" + howToxic.getNegativeCount() + "\n```", true)
			.addField("Total Score (higher is better!)",  "```\n" + howToxic.getScore() + "\n```", true)
			.addField("Average Score (higher is better!)",  "```\n" + howToxic.getAverageScore() + "\n```", true)
			.addField("Most positive member", "<@" +  topMember.getPack() + "> - `" + topMember.getScore() + "`", true)
			.addField("Most negative member", "<@" +  bottomMember.getPack() + "> - `" + bottomMember.getScore() + "`", true)
			.setFooter(getMessage(howToxic))
			.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(Sentiment howToxic) {
		return howToxic.getScore() < -2 ? "You are all terrible people go and sit in the corner and think about your actions"
				: howToxic.getScore() < 2 ? "There's okay people in here somewhere..."
				: "You are all amazingly perfect people with zero flaws";
	}

	private Color getColour(Sentiment howToxic) {
		return howToxic.getScore() < -2 ? Color.RED
				: howToxic.getScore() < 2 ? Color.ORANGE
				: Color.GREEN;
	}

	@Override
	public String getName() {
		return "howtoxicisthisserver";
	}

	@Override
	public String getUsage() {
		return "howtoxicisthisserver";
	}

	@Override
	public String getDescription() {
		return "Want to see how toxic your server is? You may not like the results...";
	}

	@Override
	public String getShorthand() {
		return "hts";
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
	public CommandCategory getCommandCategory() {
		return CommandCategory.SENTIMENT_ANALYSIS;
	}

}