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
				.addField(":grin: Average Positive Score", "```\n" + howToxic.getPositivePolarity() + "\n```", true)
				.addField(":neutral_face: Average Neutral Score", "```\n" + howToxic.getNeutralPolarity() + "\n```", true)
				.addField(":angry: Average Negative Score", "```\n" + howToxic.getNegativePolarity() + "\n```", true)
				.addField("Compound Score", "```\n" + howToxic.getCompoundPolarity() + "\n```", false)
			.addField("Most positive member", "<@" +  topMember.getPack() + "> - `" + topMember.getCompoundPolarity() + "`", true)
			.addField("Most negative member", "<@" +  bottomMember.getPack() + "> - `" + bottomMember.getCompoundPolarity() + "`", true)
			.setFooter(getMessage(howToxic))
			.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? "This is one of the worst servers I have ever seen"
				: howToxic.getCompoundPolarity() > 0 ? "This server is a delightfully pleasant place"
				: "This server is neither good nor bad";
	}

	private Color getColour(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? Color.RED
				: howToxic.getCompoundPolarity() > 0 ? Color.GREEN
				: Color.ORANGE;
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