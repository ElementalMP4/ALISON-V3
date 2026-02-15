package main.java.de.voidtech.alison.commands.management;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.service.SpinnerService;
import main.java.de.voidtech.alison.util.PageButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class RevealSpinnersCommand extends AbstractCommand {

    private final SpinnerService spinnerService;
    private final EventWaiter waiter;

    @Autowired
    public RevealSpinnersCommand(SpinnerService spinnerService, EventWaiter waiter) {
        this.spinnerService = spinnerService;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandContext ctx) {
        List<Spinner> revealedSpinners = spinnerService.revealSpinners(ctx.getGuild().getId(), 0);

        int page = 0;
        if (revealedSpinners.isEmpty()) {
            ctx.reply("No spinner data found.");
            return;
        }

        MessageEmbed embed = buildRevealEmbed(ctx.getGuild(), revealedSpinners, page);
        new PageButtonListener(ctx, waiter, embed, (consumer, newPage) -> {
            List<Spinner> pageData = spinnerService.revealSpinners(ctx.getGuild().getId(), newPage);

            if (pageData.isEmpty()) {
                return;
            }

            MessageEmbed updated = buildRevealEmbed(ctx.getGuild(), pageData, newPage);
            boolean hasPrev = newPage > 0;
            boolean hasNext = pageData.size() == Spinner.SPINNER_LB_PAGE_SIZE;

            consumer.edit(
                    updated,
                    PageButtonListener.createButtons(newPage, hasPrev, hasNext)
            );
        });
    }

    public MessageEmbed buildRevealEmbed(
            Guild guild,
            List<Spinner> leaderboard,
            int page) {
        StringBuilder sb = new StringBuilder();

        for (Spinner spinner : leaderboard) {
            sb.append(
                    "**<@%s> in <#%s>**\n".formatted(spinner.getUserID(), spinner.getChannelID())
            );
        }

        return new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("%s Spinner Reveal-o-matic".formatted(guild.getName()))
                .setDescription(sb.toString())
                .setFooter("Page %d".formatted(page + 1))
                .build();
    }

    @Override
    public String getName() {
        return "spinreveal";
    }

    @Override
    public String getUsage() {
        return "spinreveal";
    }

    @Override
    public String getDescription() {
        return "Reveal all spinning spinners";
    }

    @Override
    public String getShorthand() {
        return "sprev";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
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
