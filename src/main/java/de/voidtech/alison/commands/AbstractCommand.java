package main.java.de.voidtech.alison.commands;

import main.java.de.voidtech.alison.service.ConfigService;
import main.java.de.voidtech.alison.service.ThreadManager;
import main.java.de.voidtech.alison.util.Stopwatch;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractCommand {

    @Autowired
    private ThreadManager threadManager;

    @Autowired
    private ConfigService configService;

    public static final Logger LOGGER = Logger.getLogger(AbstractCommand.class.getSimpleName());

    private static final Set<ChannelType> FORBIDDEN_ZONES = Set.of(
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.GUILD_PUBLIC_THREAD
    );

    public void run(CommandContext context) {
        try {
            if (FORBIDDEN_ZONES.contains(context.getChannel().getType())  && !isMaster(context)) {
                context.reply("Commands cannot be used in threads!");
                return;
            }

            if (this.getCommandCategory() == CommandCategory.MANAGEMENT && !isMaster(context)) {
                context.reply("Only the bot master can use this command!");
                return;
            }

            if (!this.isDmCapable() && context.getChannel().getType().equals(ChannelType.PRIVATE)) {
                context.reply("This command only works in guilds!");
                return;
            }

            if (this.requiresArguments() && !context.hasArgs()) {
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

    private boolean isMaster(CommandContext context) {
        return configService.getMaster().equals(context.getAuthor().getId());
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