package main.java.de.voidtech.alison.commands.privacy;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.util.ButtonListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ClearCommand extends AbstractCommand {

	@Autowired
	private EventWaiter waiter;

	@Autowired
	private AlisonService textGenerationService;

    @Override
    public void execute(CommandContext context, List<String> args) {
    	new ButtonListener(context, waiter,"Are you sure you want to delete all your data? **This cannot be undone!**", result -> {
    		if (result.userSaidYes()) {
    			textGenerationService.delete(context.getAuthor().getId());
				result.getMessage().editMessage("Your data has been cleared! If you want to stop data collection, use the `optout` command!").queue();
    		} else {
    			result.getMessage().editMessage("Your data has been left alone for now.").queue();	
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
		return "Allows you to delete all your learnt words. If you want ALISON to stop learning, use the optout command!";
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

}