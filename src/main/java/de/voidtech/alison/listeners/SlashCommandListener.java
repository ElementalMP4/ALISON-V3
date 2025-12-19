package main.java.de.voidtech.alison.listeners;

import main.java.de.voidtech.alison.annotations.Listener;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Listener
public class SlashCommandListener extends ListenerAdapter {

    public static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getSimpleName());

    @Autowired
    private List<AbstractCommand> commands;

    @Autowired
    private JDA jda;

    @EventListener(ApplicationReadyEvent.class)
    public void registerSlashCommands() {
        for (AbstractCommand command : commands) {
            SlashCommandData cmd = Commands.slash(command.getName(), command.getDescription())
                    .addOptions(command.getSlashCommandOptions().options())
                    .addSubcommands(command.getSlashCommandOptions().subCommands());
            jda.upsertCommand(cmd).queue();
            LOGGER.log(Level.INFO, "Registered slash command " + command.getName());
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        for (AbstractCommand command : commands) {
            if (command.getName().equalsIgnoreCase(event.getName())) {
                CommandContext context = new CommandContext(event);
                command.execute(context);
                LOGGER.log(Level.INFO, "Running slash command " + command.getName() + " from user " + event.getUser().getName());
            }
        }
    }

}
