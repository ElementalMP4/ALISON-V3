package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.util.ButtonListener;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class OptOutCommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private EventWaiter waiter;
	
	@Override
	public void execute(CommandContext context) {
		if (!privacyService.userHasOptedOut(context.getAuthor().getId())) {
			privacyService.optOut(context.getAuthor().getId());

			new ButtonListener(context, waiter,"Would you like to delete your stored data?", result -> {
				if (result.userSaidYes()) {
					textGenerationService.delete(context.getAuthor().getId());
					result.editResponse("Your data has been cleared!");
				} else {
					result.editResponse("Your data has been left alone for now. Use the `clear` command if you change your mind!");
				}
			});			
			
		} else context.reply("You have already chosen to opt out!");
	}

	@Override
	public String getName() {
		return "optout";
	}

	@Override
	public String getUsage() {
		return "optout";
	}

	@Override
	public String getDescription() {
		return "Revoke ALISON's permission to read your server messages";
	}

	@Override
	public String getShorthand() {
		return "out";
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