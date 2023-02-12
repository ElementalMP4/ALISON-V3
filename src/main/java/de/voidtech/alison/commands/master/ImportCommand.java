package main.java.de.voidtech.alison.commands.master;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.IngestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ImportCommand extends AbstractCommand {

    @Autowired
    private IngestService ingestService;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (args.get(0).equals("all")) {
            ingestService.ingestClaire(commandContext);
            ingestService.ingestAlison(commandContext);
        } else if (args.get(0).equals("alison")) {
            ingestService.ingestAlison(commandContext);
        } else if (args.get(0).equals("claire")) {
            ingestService.ingestClaire(commandContext);
        } else {
            commandContext.reply("Invalid ingest mode");
        }
    }

    @Override
    public String getName() {
        return "import";
    }

    @Override
    public String getUsage() {
        return "import all\n" +
                "import alison\n" +
                "import claire";
    }

    @Override
    public String getDescription() {
        return "Allows the Bot Master to import data from an SQLite db";
    }

    @Override
    public String getShorthand() {
        return "imp";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MASTER;
    }

    @Override
    public boolean isDmCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

}