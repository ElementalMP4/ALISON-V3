package main.java.de.voidtech.alison.commands.information;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.entities.Sentiment;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.TextGenerationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class MyStatsCommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private TextGenerationService textGenerationService;

	@Autowired
	private AnalysisService analysisService;

	@Override
	public void execute(CommandContext context, List<String> args) {
		String ID;
    	if (args.isEmpty()) ID = context.getAuthor().getId();
    	else ID = args.get(0).replaceAll("([^0-9])", "");
    	
        if (ID.equals("")) context.reply("I couldn't find that user :(");
        else {
        	Result<User> userResult = context.getJDA().retrieveUserById(ID).mapToResult().complete();
            if (userResult.isSuccess()) {
            	if (privacyService.userHasOptedOut(ID)) {
            		context.reply("This user has chosen not to have their data analysed.");
            		return;
            	}
                MessageEmbed statsEmbed = createStatsEmbed(userResult.get());
                context.reply(statsEmbed);
            } else context.reply("User " + ID + " could not be found");	
        }
	}

	private MessageEmbed createStatsEmbed(User user) {
		Sentiment sentiment = analysisService.analyseCollection(user.getId());
        long wordCount = textGenerationService.getWordCountForCollection(user.getId());
        EmbedBuilder statsEmbedBuilder = new EmbedBuilder()
        		.setColor(Color.ORANGE)
        		.setTitle("Stats for " + user.getAsTag())
        		.setThumbnail(user.getAvatarUrl())
        		.addField("Total Words", "```\n" + wordCount + "\n```", false)
				.addField("Sentiment Score", "```\n" + sentiment.getScore() + "\n```", false);
		return statsEmbedBuilder.build();
	}

	@Override
	public String getName() {
		return "mystats";
	}

	@Override
	public String getUsage() {
		return "mystats\n"
				+ "mystats [user mention]";
	}

	@Override
	public String getDescription() {
		return "Shows some stats about you or someone else";
	}

	@Override
	public String getShorthand() {
		return "me";
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