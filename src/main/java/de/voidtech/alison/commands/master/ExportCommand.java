package main.java.de.voidtech.alison.commands.master;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.IngestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ExportCommand extends AbstractCommand {

    @Autowired
    private IngestService ingestService;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (args.get(0).equals("all")) {
            ingestService.exportClaire();
            ingestService.exportAlison();
        } else if (args.get(0).equals("alison")) {
            ingestService.exportAlison();
        } else if (args.get(0).equals("claire")) {
            ingestService.exportClaire();
        } else {
            commandContext.reply("Invalid export mode");
        }
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public String getUsage() {
        return "export all\n" +
                "export alison\n" +
                "export claire";
    }

    @Override
    public String getDescription() {
        return "Allows the Bot Master to export data to an SQLite db";
    }

    @Override
    public String getShorthand() {
        return "exp";
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