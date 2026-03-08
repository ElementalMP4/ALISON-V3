package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.interaction.TrueFalseButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Command
public class ClearCommand extends AbstractCommand {

	private final EventWaiter waiter;
	private final AlisonService textGenerationService;

	@Autowired
	public ClearCommand(EventWaiter waiter, AlisonService textGenerationService) {
		this.waiter = waiter;
		this.textGenerationService = textGenerationService;
	}

    @Override
	protected void execute(CommandContext context) {
		MessageEmbed askEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Delete Data")
				.setDescription("Are you sure you want to delete all your data? **This cannot be undone!**")
				.build();
    	new TrueFalseButtonListener(context, waiter, askEmbed, result -> {
    		if (result.userSaidYes()) {
				textGenerationService.delete(context.getAuthor().getId());
				MessageEmbed resp = new EmbedBuilder()
						.setTitle("Data Deleted")
						.setColor(Color.GREEN)
						.setDescription("Your data has been cleared! If you want to stop data collection, use the `optout` command!")
						.build();
				result.editResponse(resp);
    		} else {
				MessageEmbed resp = new EmbedBuilder()
						.setTitle("Data Not Deleted")
						.setColor(Color.RED)
						.setDescription("Your data will not be deleted. If you want to stop data collection without deleting your data, use the `optout` command!")
						.build();
    			result.editResponse(resp);
    		}
		});		
    }
    
    @Override
    public String getName() {
        return "clear";
    }

	@Override
	public String getUsage() {
		return "clear";
	}

	@Override
	public String getDescription() {
		return "Delete all of your data from the ALISON database. This is irreversible, and has immediate effect.";
	}

	@Override
	public String getShorthand() {
		return "c";
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