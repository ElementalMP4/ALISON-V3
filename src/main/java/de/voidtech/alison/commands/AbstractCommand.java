package main.java.de.voidtech.alison.commands;

import main.java.de.voidtech.alison.service.ThreadManager;
import main.java.de.voidtech.alison.util.Stopwatch;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCommand {

    @Autowired
    private ThreadManager threadManager;

    public static final Logger LOGGER = Logger.getLogger(AbstractCommand.class.getSimpleName());

    public void run(CommandContext context) {
        try {
            if (!this.isDmCapable() && context.getChannel().getType().equals(ChannelType.PRIVATE)) {
                context.reply("This command only works in guilds!");
                return;
            }

            if (!context.isSlashCommand() && this.requiresArguments() && context.getArgs().isEmpty()) {
                context.reply("This command needs arguments but you didn't supply any!\n" + this.getUsage());
                return;
            }

            ExecutorService commandExecutor = threadManager.getThreadByName("T-Command");
            Runnable commandThread = () -> {
                Stopwatch stopwatch = new Stopwatch().start();
                LOGGER.log(Level.INFO, "Running command: " + this.getName() + " by " + context.getAuthor().getName());
                execute(context);
                LOGGER.log(Level.INFO, "Command " + this.getName() + " by " + context.getAuthor().getName()
                        + " Took " + stopwatch.stop().getTime() + "ms");
            };
            commandExecutor.execute(commandThread);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing command: " + this.getName(), e);
            context.reply("An error has occurred whilst running this command: " + e.getMessage());
        }
    }

    protected abstract void execute(CommandContext commandContext);

    public abstract String getName();

    public abstract String getUsage();

    public abstract String getDescription();

    public abstract String getShorthand();

    public abstract CommandCategory getCommandCategory();

    public abstract boolean isDmCapable();

    public abstract boolean requiresArguments();

    public abstract SlashCommandOptions getSlashCommandOptions();
}