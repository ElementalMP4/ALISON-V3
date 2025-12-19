package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.AnalysisService;
import main.java.de.voidtech.alison.vader.analyser.SentimentPolarities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Command
public class ToxicityLeaderboardCommand extends AbstractCommand {

	@Autowired
	private AnalysisService analyser;

	@Override
	protected void execute(CommandContext context) {
		List<SentimentPolarities> allMembers = analyser.analyseServer(context.getGuild());
		String leaderboard = createLeaderboardString(allMembers);
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Toxicity leaderboard for " + context.getGuild().getName())
				.setDescription(leaderboard)
				.build();
		context.reply(leaderboardEmbed);
	}
	
	private String createLeaderboardString(List<SentimentPolarities> allMembers) {
		StringBuilder leaderboard = new StringBuilder("**Top 5 members**\n");
		List<SentimentPolarities> topFive = allMembers.stream().limit(5).collect(Collectors.toList());
		Collections.reverse(allMembers);
		List<SentimentPolarities> bottomFive = allMembers.stream().limit(5).collect(Collectors.toList());
		Collections.reverse(allMembers);
		Collections.reverse(bottomFive);
		
		for (SentimentPolarities sentiment : topFive) {
			leaderboard.append(intToEmojiString(allMembers.indexOf(sentiment) + 1));
			leaderboard.append("<@").append(sentiment.getPack()).append("> - `");
			leaderboard.append(sentiment.getCompoundPolarity()).append("`\n");
		}
		leaderboard.append("**Bottom 5 Members**\n");
		for (SentimentPolarities sentiment: bottomFive) {
			leaderboard.append(intToEmojiString(allMembers.indexOf(sentiment) + 1));
			leaderboard.append("<@").append(sentiment.getPack()).append("> - `");
			leaderboard.append(sentiment.getCompoundPolarity()).append("`\n");
		}
		
		return leaderboard.toString();
	}

	public static String intToEmojiString(int position) {
		String digits = String.valueOf(position);
		StringBuilder result = new StringBuilder();
		for (String digit : digits.split("")) {
			switch (digit) {
				case "1":
					result.append(":one:");
					break;
				case "2":
					result.append(":two:");
					break;
				case "3":
					result.append(":three:");
					break;
				case "4":
					result.append(":four:");
					break;
				case "5":
					result.append(":five:");
					break;
				case "6":
					result.append(":six:");
					break;
				case "7":
					result.append(":seven:");
					break;
				case "8":
					result.append(":eight:");
					break;
				case "9":
					result.append(":nine:");
					break;
				case "10":
					result.append(":ten:");
					break;
				default:
					result.append(":zero:");
					break;
			}
		}
		return result.toString();
	}

	@Override
	public String getName() {
		return "toxicitylb";
	}

	@Override
	public String getUsage() {
		return "toxicitylb";
	}

	@Override
	public String getDescription() {
		return "Allows you to see 5 most and 5 least toxic server members";
	}

	@Override
	public String getShorthand() {
		return "toxlb";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.SENTIMENT_ANALYSIS;
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
		return new SlashCommandOptions();
	}

}