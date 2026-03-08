package main.java.de.voidtech.alison.commands.spinning;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.service.SpinnerService;
import main.java.de.voidtech.alison.interaction.PageButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class SpinLeaderboardCommand extends AbstractCommand {

    private final SpinnerService spinnerService;
    private final EventWaiter waiter;

    @Autowired
    public SpinLeaderboardCommand(final SpinnerService spinnerService, final EventWaiter waiter) {
        this.spinnerService = spinnerService;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandContext ctx) {
        int page = 0;

        long pages = spinnerService.getNumberOfLeaderboardPages(ctx.getGuild().getId());
        List<Spinner> spinners = spinnerService.getServerLeaderboard(ctx.getGuild().getId(), page);

        if (spinners.isEmpty()) {
            ctx.reply("No spinner data found.");
            return;
        }

        MessageEmbed embed = buildLeaderboardEmbed(ctx.getGuild(), spinners, page, pages);
        new PageButtonListener(ctx, waiter, embed, (consumer, newPage) -> {
            List<Spinner> pageData = spinnerService.getServerLeaderboard(ctx.getGuild().getId(), newPage);

            if (pageData.isEmpty()) {
                return;
            }

            MessageEmbed updated = buildLeaderboardEmbed(ctx.getGuild(), pageData, newPage, pages);
            boolean hasPrev = newPage > 0;
            boolean hasNext = pageData.size() == Spinner.SPINNER_LB_PAGE_SIZE;

            consumer.edit(
                    updated,
                    PageButtonListener.createButtons(newPage, hasPrev, hasNext)
            );
        });
    }

    public MessageEmbed buildLeaderboardEmbed(
            Guild guild,
            List<Spinner> leaderboard,
            int page,
            long pages
    ) {
        StringBuilder sb = new StringBuilder();
        int pos = page * Spinner.SPINNER_LB_PAGE_SIZE + 1;

        for (Spinner spinner : leaderboard) {
            sb.append(
                    "**%s – <@%s> in %s**\n".formatted(formatPosition(pos++), spinner.getUserID(), spinner.getChannelForLeaderboard())
            );
            sb.append("```\n");
            sb.append("Lasted: %s\n".formatted(spinner.durationAsText()));
            sb.append("```\n\n");
        }

        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("%s Spinner Leaderboard".formatted(guild.getName()))
                .setDescription(sb.toString())
                .setFooter("Page %d of %d".formatted(page + 1, pages))
                .build();
    }

    private String formatPosition(int pos) {
        return switch (pos) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.valueOf(pos);
        };
    }

    @Override
    public String getName() {
        return "spinlb";
    }

    @Override
    public String getUsage() {
        return "spinlb";
    }

    @Override
    public String getDescription() {
        return "View the spinner leaderboard";
    }

    @Override
    public String getShorthand() {
        return "spinlb";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.SPINNING;
    }

    @Override
    public boolean isDmCapable() {
        return false;
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
