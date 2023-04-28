package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.vader.analyser.SentimentPolarities;
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
		SentimentPolarities sentiment = analyser.analyseCollection(user.getId());
		if (sentiment == null) {
			context.reply("I couldn't find any data to analyse!");
			return;
		}
		MessageEmbed toxicityEmbed = new EmbedBuilder()
				.setColor(getColour(sentiment))
				.setTitle("How toxic is " + user.getName() + "?")
				.addField(":grin: Positive Score", "```\n" + sentiment.getPositivePolarity() + "\n```", false)
				.addField(":neutral_face: Neutral Score", "```\n" + sentiment.getNeutralPolarity() + "\n```", false)
				.addField(":angry: Negative Score", "```\n" + sentiment.getNegativePolarity() + "\n```", false)
				.addField("Compound Score", "```\n" + sentiment.getCompoundPolarity() + "\n```", false)
				.setFooter(getMessage(sentiment))
				.build();
		context.reply(toxicityEmbed);
	}

	private String getMessage(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? "You are known by the state of california to cause crippling sadness"
				: howToxic.getCompoundPolarity() > 0 ? "You are positively epic"
				: "You are positively unobjectionable";
	}

	private Color getColour(SentimentPolarities howToxic) {
		return howToxic.getCompoundPolarity() < 0 ? Color.RED
				: howToxic.getCompoundPolarity() > 0 ? Color.GREEN
				: Color.ORANGE;
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