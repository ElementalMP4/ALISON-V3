package main.java.de.voidtech.alison.commands.generation;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.util.ButtonConsumer;
import main.java.de.voidtech.alison.util.ButtonListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class NicknameCommand extends AbstractCommand {

	@Autowired
	private PrivacyService privacyService;

	@Autowired
	private AlisonService textGenerationService;

	@Autowired
	private EventWaiter waiter;

	@Override
	public void execute(CommandContext context, List<String> args) {
		Member member = null;
    	if (args.isEmpty()) member = context.getMember();
    	else {
    		if (!context.getMember().getPermissions().contains(Permission.NICKNAME_MANAGE)) {
    			context.reply("You don't have permission to change other people's nicknames!");
				return;
    		}

			Result<Member> memberResult = context.getGuild().retrieveMemberById(args.get(0).replaceAll("([^0-9])", "")).mapToResult().complete();
    		if (!memberResult.isSuccess()) {
    			context.reply("I couldn't find that user :(");
				return;
    		} else member = memberResult.get();

			if (privacyService.userHasOptedOut(member.getId())) {
				context.replyErrorEmbed("This user has chosen not to be imitated.");
				return;
			}
    	}

		String nickname = textGenerationService.createNickname(member.getId());
		if (nickname == null) context.reply("I don't have enough information to make a nickname :(");
		else {
			Member finalMember = member;
			new ButtonListener(context, waiter,
					"Change **" + member.getUser().getName() + "'s** nickname to **" + nickname + "**?",
					result -> handleNicknameUpdateChoice(finalMember, nickname, context, result));
		}
	}
	
	private void handleNicknameUpdateChoice(Member member, String nickname, CommandContext context, ButtonConsumer result ) {
		if (result.userSaidYes()) {
			if (member.isOwner()) {
	    		result.getMessage().editMessage("I can't change the owner's nickname! The nickname I generated for you was **" + nickname + "**").queue();
	    		return;
	    	}
			if (!context.getGuild().getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
				result.getMessage().editMessage("I don't have permission to change nicknames! Please make sure I have the `Manage Nicknames` permission! The nickname I generated for you was **" + nickname + "**").queue();
				return;
			}
			if (!context.getGuild().getSelfMember().canInteract(member)) {
				result.getMessage().editMessage("I don't have permission to change **" + member.getUser().getName() +
						"'s**  nickname! I need my role to be above **" + member.getUser().getName() + "'s** highest role! The nickname I generated for you was **" + nickname + "**").queue();
				return;
			}
			String oldNickname = member.getEffectiveName();
			member.modifyNickname(nickname).complete();
			result.getMessage().editMessage("**" + member.getUser().getName() + "'s** Nickname changed to **" + nickname + "** from **" + oldNickname + "**").queue();
		} else {
			result.getMessage().editMessage("**" + member.getUser().getName() + "'s** Nickname has not been changed to **" + nickname + "**").queue();
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
		return "Get Alison to generate a nickname for you based on things you've said";
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
}