package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.util.ButtonConsumer;
import main.java.de.voidtech.alison.util.ButtonListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class NicknameCommand extends AbstractCommand {

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private AlisonService textGenerationService;

    @Autowired
    private EventWaiter waiter;

    @Override
    public void execute(CommandContext context) {
        if (privacyService.userHasOptedOut(context.getMember().getId())) {
            context.replyErrorEmbed("You have opted out, so a name cannot be generated");
            return;
        }

        String nickname = textGenerationService.createNickname(context.getMember().getId());
        if (nickname == null) context.reply("I don't have enough information to make a nickname for you :(");
        else {

            if (context.isSlashCommand()) {

            } else {
                new ButtonListener(context, waiter,
                        "Change **" + context.getMember().getUser().getName() + "'s** nickname to **" + nickname + "**?",
                        result -> handleNicknameUpdateChoice(context.getMember(), nickname, context, result));
            }
        }
    }

    private void handleNicknameUpdateChoice(Member member, String nickname, CommandContext context, ButtonConsumer result) {
        if (result.userSaidYes()) {
            if (member.isOwner()) {
                result.editResponse("I can't change the owner's nickname! The nickname I generated for you was **" + nickname + "**");
                return;
            }
            if (!context.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
                result.editResponse("I don't have permission to change nicknames! Please make sure I have the `Manage Nicknames` permission! The nickname I generated for you was **" + nickname + "**");
                return;
            }
            if (!context.getGuild().getSelfMember().canInteract(member)) {
                result.editResponse("I don't have permission to change your nickname! I need my role to be above your highest role! The nickname I generated for you was **" + nickname + "**");
                return;
            }
            String oldNickname = member.getEffectiveName();
            member.modifyNickname(nickname).complete();
            result.editResponse("Your nickname has been changed to **" + nickname + "** from **" + oldNickname + "**");
        } else {
            result.editResponse("I won't change your nickname to **" + nickname + "**");
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