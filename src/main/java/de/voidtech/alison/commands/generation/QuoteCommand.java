package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.ImageService;
import main.java.de.voidtech.alison.service.PrivacyService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;

@Command
public class QuoteCommand extends AbstractCommand {

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private ImageService imageService;

	@Override
	public void execute(CommandContext context) {
		String ID;

		if (context.isSlashCommand()) {
			if (context.getEvent().getOption("user") == null) ID = context.getAuthor().getId();
			else ID = context.getEvent().getOption("user").getAsUser().getId();
		} else {
			if (context.getArgs().isEmpty()) ID = context.getAuthor().getId();
			else ID = context.getArgs().get(0).replaceAll("([^0-9a-zA-Z])", "");
		}

		if (!textGenerationService.dataIsAvailableForID(ID)) {
			context.reply("I couldn't find any data for that user :(");
			return;
		}

		if (privacyService.userHasOptedOut(ID)) {
			context.reply("This user has chosen not to be quoted.");
			return;
		}

		String quote = textGenerationService.createQuote(ID);
		if (quote == null) {
			context.reply("I couldn't find any data for that user :(");
			return;
		}
		
		Result<Member> userResult = context.getGuild().retrieveMemberById(ID).mapToResult().complete();
		if (userResult.isSuccess()) {
			User user = userResult.get().getUser();

			byte[] image = getQuoteImage(user, quote);
			if (image == null) {
				context.reply("Failed to generate image");
				return;
			}

			MessageEmbed quoteEmbed = new EmbedBuilder()
					.setTitle("An infamous quote from " + user.getName())
					.setColor(Color.ORANGE)
					.setImage("attachment://quote.png")
					.build();
			context.replyWithFile(image, "quote.png", quoteEmbed);
		} else context.reply("I couldn't find that user :(");
	}

	private byte[] getQuoteImage(User user, String quote) {
		try {
			return imageService.createQuoteImage(user.getEffectiveAvatarUrl(), user.getName(), quote);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getName() {
		return "quote";
	}

	@Override
	public String getUsage() {
		return "quote";
	}

	@Override
	public String getDescription() {
		return "Generate an infamous quote for yourself or somebody else";
	}

	@Override
	public String getShorthand() {
		return "q";
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
		return CommandCategory.TEXT_GENERATION;
	}

	@Override
	public SlashCommandOptions getSlashCommandOptions() {
		return new SlashCommandOptions(new OptionData(OptionType.USER, "user", "The user to create a quote for", false));
	}

}