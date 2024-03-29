package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.BrowserService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.AlisonService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Command
public class SearchCommand extends AbstractCommand {

	private static final String SEARCH_URL = "https://duckduckgo.com/?ia=web&iax=web&q=";
	private static final String SAFE_MODE_ENABLED = "&kp=1";
	private static final String SAFE_MODE_DISABLED = "&kp=-2";
	private static final String MISC_OPTS_SUFFIX = "&kae=d&k9=b";

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private BrowserService browser;
	
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

		String search = textGenerationService.createSearch(ID);
		if (search == null) {
			context.reply("I couldn't find any data for that user :(");
			return;
		}
		
		Result<Member> userResult = context.getGuild().retrieveMemberById(ID).mapToResult().complete();
		if (userResult.isSuccess()) {
			User user = userResult.get().getUser();
			String url = SEARCH_URL + URLEncoder.encode(search)
					+ (canSendNsfw(context.getMessage().getChannel()) ? SAFE_MODE_DISABLED : SAFE_MODE_ENABLED) 
					+ MISC_OPTS_SUFFIX;
			byte[] image = browser.searchAndScreenshot(url);
			MessageEmbed searchEmbed = new EmbedBuilder()
					.setTitle(user.getName() + "'s search history probably contains:", url)
					.setColor(Color.ORANGE)
					.setImage("attachment://search.png")
					.build();
			context.replyWithFile(image, "search.png", searchEmbed);
		} else context.reply("I couldn't find that user :(");
	}
	
	private boolean canSendNsfw(MessageChannel messageChannel) {
		return messageChannel.getType().equals(ChannelType.PRIVATE) || ((TextChannel) messageChannel).isNSFW();
	}

	@Override
	public String getName() {
		return "search";
	}

	@Override
	public String getUsage() {
		return "search";
	}

	@Override
	public String getDescription() {
		return "Alison will search the internet for some of your greatest thoughts";
	}

	@Override
	public String getShorthand() {
		return "s";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.TEXT_GENERATION;
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