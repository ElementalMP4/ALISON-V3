package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.util.TrueFalseButtonConsumer;
import main.java.de.voidtech.alison.util.TrueFalseButtonListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@Command
public class NicknameCommand extends AbstractCommand {

    private final PrivacyService privacyService;
    private final AlisonService textGenerationService;
    private final EventWaiter waiter;

    @Autowired
    public NicknameCommand(PrivacyService privacyService, AlisonService textGenerationService, EventWaiter waiter) {
        this.privacyService = privacyService;
        this.textGenerationService = textGenerationService;
        this.waiter = waiter;
    }

    @Override
    protected void execute(CommandContext context) {
        if (privacyService.userHasOptedOut(context.getMember().getId())) {
            context.replyErrorEmbed("You have opted out, so a name cannot be generated");
            return;
        }

        String nickname = textGenerationService.createNickname(context.getMember().getId());
        if (nickname == null) context.replyErrorEmbed("I don't have enough information to make a nickname for you :(");
        else {
            MessageEmbed askEmbed = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle("Change Nickname")
                    .setDescription("Change your nickname to **" + nickname + "**?")
                    .build();
            new TrueFalseButtonListener(context, waiter, askEmbed,
                    result -> handleNicknameUpdateChoice(context.getMember(), nickname, context, result));
        }
    }

    private void handleNicknameUpdateChoice(Member member, String nickname, CommandContext context, TrueFalseButtonConsumer result) {
        if (result.userSaidYes()) {
            if (member.isOwner()) {
                MessageEmbed resp = new EmbedBuilder()
                        .setTitle("Nickname Unchanged")
                        .setColor(Color.RED)
                        .setDescription("I can't change the owner's nickname! The nickname I generated for you was **" + nickname + "**")
                        .build();
                result.editResponse(resp);
                return;
            }
            if (!context.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
                MessageEmbed resp = new EmbedBuilder()
                        .setTitle("Missing Permissions")
                        .setColor(Color.RED)
                        .setDescription("I don't have permission to change nicknames! Please make sure I have the `Manage Nicknames` permission! The nickname I generated for you was **" + nickname + "**")
                        .build();
                result.editResponse(resp);
                return;
            }
            if (!context.getGuild().getSelfMember().canInteract(member)) {
                MessageEmbed resp = new EmbedBuilder()
                        .setTitle("Missing Permissions")
                        .setColor(Color.RED)
                        .setDescription("I don't have permission to change your nickname! I need my role to be above your highest role! The nickname I generated for you was **" + nickname + "**")
                        .build();
                result.editResponse(resp);
                return;
            }

            String oldNickname = member.getEffectiveName();
            member.modifyNickname(nickname).complete();

            MessageEmbed resp = new EmbedBuilder()
                    .setTitle("Nickname Updated")
                    .setColor(Color.GREEN)
                    .setDescription("Your nickname has been changed to **" + nickname + "** from **" + oldNickname + "**")
                    .build();
            result.editResponse(resp);
        } else {
            MessageEmbed resp = new EmbedBuilder()
                    .setTitle("Nickname Unchanged")
                    .setColor(Color.GRAY)
                    .setDescription("I won't change your nickname to **" + nickname + "**")
                    .build();
            result.editResponse(resp);
        }
    }

    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    public String getUsage() {
        return "nickname";
    }

    @Override
    public String getDescription() {
        return "Get ALISON to generate a nickname for you";
    }

    @Override
    public String getShorthand() {
        return "nn";
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
    public CommandCategory getCommandCategory() {
        return CommandCategory.TEXT_GENERATION;
    }

    @Override
    public SlashCommandOptions getSlashCommandOptions() {
        return new SlashCommandOptions();
    }
}