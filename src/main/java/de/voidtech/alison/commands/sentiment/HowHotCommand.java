package main.java.de.voidtech.alison.commands.sentiment;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.ConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Random;

@Command
public class HowHotCommand extends AbstractCommand {

	@Autowired
	private ConfigService config;

	@Override
	public void execute(CommandContext context, List<String> args) {
		String ID;
		if (args.isEmpty()) ID = context.getAuthor().getId();
		else ID = args.get(0).replaceAll("([^0-9a-zA-Z])", "");
		Result<User> userResult = context.getJDA().retrieveUserById(ID).mapToResult().complete();
		if (userResult.isSuccess()) {
			int rating = getRating(userResult.get());
			MessageEmbed hotnessEmbed = new EmbedBuilder()
					.setColor(getColor(rating))
					.setTitle("I rate you a " + rating + " out of 10. " + getPhrase(rating))
					.setImage(userResult.get().getAvatarUrl() + "?size=2048")
					.build();
			context.reply(hotnessEmbed);
		} else context.reply("I couldn't find that user :(");	
	}
	
	private int getRating(User user) {
		return user.getId().equals(config.getMaster()) ? 10 :
			new Random(user.getAvatarId().hashCode()).nextInt(10);
	}

	private Color getColor(int rating)
	{
		return rating > 6 
				? Color.GREEN 
				: rating > 3 
				? Color.ORANGE 
				: Color.RED;
	}
	
	private String getPhrase(int rating)
	{
		return rating > 6
				? "What's cookin' good lookin' :smirk:" 
				: rating > 3
				? "Not too shabby..." 
				: "I may need to bleach my eyes.";
	}

	@Override
	public String getName() {
		return "howhotami";
	}

	@Override
	public String getUsage() {
		return "howhotami\n"
				+ "howhotami [user ID/mention]";
	}

	@Override
	public String getDescription() {
		return "See how hot you are (based on your pfp)! (ALISON will be very honest. You may not like the result.)";
	}

	@Override
	public String getShorthand() {
		return "hhme";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.SENTIMENT_ANALYSIS;
	}

	@Override
	public boolean isDmCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}
}