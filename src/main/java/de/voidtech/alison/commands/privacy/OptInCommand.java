package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.PrivacyService;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class OptInCommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Override
	public void execute(CommandContext context) {
		String ID = context.getAuthor().getId();
		if (privacyService.userHasOptedOut(ID)) {
			context.reply("You have been re-opted in to the learning program! I will learn from your messages again!");
			privacyService.optIn(ID);
		} else context.reply("You have already opted in to the learning program! (Users are opted in by default!)");
	}

	@Override
	public String getName() {
		return "optin";
	}

	@Override
	public String getUsage() {
		return "optin";
	}

	@Override
	public String getDescription() {
		return "Give ALISON permission to read your server messages";
	}

	@Override
	public String getShorthand() {
		return "in";
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
		return CommandCategory.PRIVACY;
	}

	@Override
	public SlashCommandOptions getSlashCommandOptions() {
		return new SlashCommandOptions();
	}

}