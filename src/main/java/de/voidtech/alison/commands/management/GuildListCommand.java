package main.java.de.voidtech.alison.commands.management;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

@Command
public class GuildListCommand extends AbstractCommand {

    @Override
    protected void execute(CommandContext commandContext) {
        EmbedBuilder embed = new EmbedBuilder();
        for (Guild guild : commandContext.getJDA().getGuilds()) {
            embed.addField(guild.getName(), guild.getId() + "\n" + guild.getDescription(), false);
        }

        commandContext.reply(embed.build());
    }

    @Override
    public String getName() {
        return "guildlist";
    }

    @Override
    public String getUsage() {
        return "guildlist";
    }

    @Override
    public String getDescription() {
        return "Lists guilds ALISON is in";
    }

    @Override
    public String getShorthand() {
        return "gl";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
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
    public SlashCommandOptions getSlashCommandOptions() {
        return new SlashCommandOptions();
    }
}
