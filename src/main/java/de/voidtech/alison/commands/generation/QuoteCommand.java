package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.ConfigService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.AlisonService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.Result;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Command
public class QuoteCommand extends AbstractCommand {

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private ConfigService config;

	@Override
	public void execute(CommandContext context, List<String> args) {		
		String ID;
		if (args.isEmpty()) ID = context.getAuthor().getId();
		else ID = args.get(0).replaceAll("([^0-9a-zA-Z])", "");

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
			String cardURL = config.getApiUrl() + "quote/?avatar_url=" + user.getEffectiveAvatarUrl() + "?size=2048"
					+ "&username=" + URLEncoder.encode(user.getName(), StandardCharsets.UTF_8.toString())
					+ "&quote=" + URLEncoder.encode(quote, StandardCharsets.UTF_8.toString());
			URL url = new URL(cardURL);
			//Remove the data:image/png;base64 part
			String response = Jsoup.connect(url.toString()).get().toString().split(",")[1];
			return DatatypeConverter.parseBase64Binary(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
		return "Create an image with a quote from either yourself or a member of your server!";
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

}