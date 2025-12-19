package main.java.de.voidtech.alison.commands.privacy;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.util.ButtonListener;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class ClearCommand extends AbstractCommand {

	@Autowired
	private EventWaiter waiter;

	@Autowired
	private AlisonService textGenerationService;

    @Override
	protected void execute(CommandContext context) {
    	new ButtonListener(context, waiter,"Are you sure you want to delete all your data? **This cannot be undone!**", result -> {
    		if (result.userSaidYes()) {
    			textGenerationService.delete(context.getAuthor().getId());
				result.editResponse("Your data has been cleared! If you want to stop data collection, use the `optout` command!");
    		} else {
    			result.editResponse("Your data has been left alone for now.");
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
		return "Delete all of your data from ALISON's dataset";
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