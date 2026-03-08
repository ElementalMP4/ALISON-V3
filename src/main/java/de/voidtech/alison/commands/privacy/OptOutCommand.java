package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.util.TrueFalseButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Command
public class OptOutCommand extends AbstractCommand {

	private final PrivacyService privacyService;
	private final AlisonService textGenerationService;
	private final EventWaiter waiter;

	@Autowired
	public OptOutCommand(PrivacyService privacyService, AlisonService textGenerationService, EventWaiter waiter) {
		this.privacyService = privacyService;
		this.textGenerationService = textGenerationService;
		this.waiter = waiter;
	}
	
	@Override
	protected void execute(CommandContext context) {
		if (!privacyService.userHasOptedOut(context.getAuthor().getId())) {
			privacyService.optOut(context.getAuthor().getId());

			MessageEmbed askEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Also delete data?")
					.setDescription("You have now been opted out of data collection. Would you also like any data we have to be deleted? **This cannot be undone!**")
					.build();
			new TrueFalseButtonListener(context, waiter,askEmbed, result -> {
				if (result.userSaidYes()) {
					textGenerationService.delete(context.getAuthor().getId());
					MessageEmbed resp = new EmbedBuilder()
							.setTitle("Data Deleted")
							.setColor(Color.GREEN)
							.setDescription("Your data has been cleared, and you have been opted out of data collection!")
							.build();
					result.editResponse(resp);
				} else {
					MessageEmbed resp = new EmbedBuilder()
							.setTitle("Opted Out")
							.setColor(Color.GREEN)
							.setDescription("You have been opted out of data collection. If you also want to delete your data, use the `clear` command!")
							.build();
					result.editResponse(resp);				}
			});			
			
		} else context.replyErrorEmbed("You have already chosen to opt out!");
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