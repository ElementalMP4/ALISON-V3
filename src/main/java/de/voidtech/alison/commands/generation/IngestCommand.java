package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.ConfigService;
import main.java.de.voidtech.alison.service.IngestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class IngestCommand extends AbstractCommand {

    @Autowired
    private IngestService ingestService;

    @Autowired
    private ConfigService config;

    @Override
    public void execute(CommandContext commandContext, List<String> args) {
        if (commandContext.getAuthor().getId().equals(config.getMaster())) {
            if (args.get(0).equals("all")) {
                ingestService.ingestFiles(commandContext);
                ingestService.ingestClaireDB(commandContext);
            } else if (args.get(0).equals("models")) {
                ingestService.ingestFiles(commandContext);
            } else if (args.get(0).equals("claire")) {
                ingestService.ingestClaireDB(commandContext);
            } else {
                commandContext.reply("Invalid ingest mode");
            }
        }
    }

    @Override
    public String getName() {
        return "ingest";
    }

    @Override
    public String getUsage() {
        return "ingest all\n" +
                "ingest models\n" +
                "ingest claire";
    }

    @Override
    public String getDescription() {
        return "Allows the Bot Master to ingest words.alison files and the Alison.db into the new PostgreSQL database";
    }

    @Override
    public String getShorthand() {
        return "ing";
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
        return true;
    }

}