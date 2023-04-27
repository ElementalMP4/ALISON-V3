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
public class HowToxicIsThisServerCommand extends AbstractCommand {

	@Autowired
	private AnalysisService analyser;

	@Override
	public void execute(CommandContext context, List<String> args) {
		List<SentimentPolarities> everyMemberJudgedIntensely = analyser.analyseServer(context.getGuild());
        SentimentPolarities topMember = everyMemberJudgedIntensely.get(0);
		SentimentPolarities bottomMember = everyMemberJudgedIntensely.get(everyMemberJudgedIntensely.size() - 1);
		SentimentPolarities howToxic = analyser.averageSentiment(everyMemberJudgedIntensely);
		MessageEmbed toxicityEmbed = new EmbedBuilder()
			.setColor(getColour(howToxic))
			.setTitle("How toxic is " + context.getGuild().getName() + "?")
			.setDescription("I judged `" + everyMemberJudgedIntensely.size() + "/" + context.getGuild().getMemberCount() + "` members")
				.addField(":grin: Average Positive Score", "```\n" + howToxic.getPositivePolarity() + "\n```", false)
				.addField(":neutral_face: Average Neutral Score", "```\n" + howToxic.getNeutralPolarity() + "\n```", false)
				.addField(":angry: Average Negative Score", "```\n" + howToxic.getNegativePolarity() + "\n```", false)
				.addField("Compound Score", "```\n" + howToxic.getCompoundPolarity() + "\n```", false)
			.addField("Most positive member", "<@" +  topMember.getPack() + "> - `" + topMember.getCompoundPolarity() + "`", true)
			.addField("Most negative member", "<@" +  bottomMember.getPack() + "> - `" + bottomMember.getCompoundPolarity() + "`", true)
			.setFooter(getMessage(howToxic))
			.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(SentimentPolarities s) {
		float neutral = s.getNeutralPolarity();
		float positive = s.getPositivePolarity();
		float negative = s.getNegativePolarity();
		if (positive > neutral && positive > negative) return "You are all amazingly perfect people with zero flaws";
		if (neutral > positive && neutral > negative) return "There's okay people in here somewhere...";
		if (negative > positive && negative > neutral) return "You are all terrible people go and sit in the corner and think about your actions";
		return "I have no idea how to judge you.";
	}

	private Color getColour(SentimentPolarities s) {
		float neutral = s.getNeutralPolarity();
		float positive = s.getPositivePolarity();
		float negative = s.getNegativePolarity();
		if (positive > neutral && positive > negative) return Color.GREEN;
		if (neutral > positive && neutral > negative) return Color.ORANGE;
		if (negative > positive && negative > neutral) return Color.RED;
		return Color.GRAY;
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