package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.WebhookManager;
import main.java.de.voidtech.alison.util.ParsingUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ImitateCommand extends AbstractCommand {

    @Autowired
    private AlisonService textService;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private WebhookManager webhookManager;

    @Override
    public void execute(CommandContext context, List<String> args) {
        String ID;
        if (args.isEmpty()) ID = context.getAuthor().getId();
        else ID = args.get(0).replaceAll("([^0-9a-zA-Z])", "");

        if (!textService.dataIsAvailableForID(ID)) {
            context.replyErrorEmbed("I couldn't find any data for that user :(");
            return;
        }

        if (privacyService.userHasOptedOut(ID)) {
            context.replyErrorEmbed("This user has chosen not to be imitated.");
            return;
        }

        String msg = textService.createImitate(ID);
        if (msg == null) {
            context.replyErrorEmbed("I couldn't find any data to make an imitation: (");
            return;
        }

        Webhook hook = webhookManager.getOrCreateWebhook(context.getMessage().getChannel().asTextChannel(), "ALISON",
                context.getJDA().getSelfUser().getId());

        if (ParsingUtils.isSnowflake(ID)) {
            Result<User> userResult = context.getJDA().retrieveUserById(ID).mapToResult().complete();
            if (userResult.isSuccess())	webhookManager.sendWebhookMessage(hook, msg,
                    userResult.get().getName(), userResult.get().getAvatarUrl());
            else context.replyErrorEmbed("I couldn't find that user :(");
        } else context.reply(msg);
    }

    @Override
    public String getName() {
        return "imitate";
    }

    @Override
    public String getUsage() {
        return "imitate [user mention or ID]";
    }

    @Override
    public String getDescription() {
        return "Allows you to use the power of ALISON to imitate someone! ALISON constantly learns from your messages,"
                + " and when you use this command, she uses her knowledge to try and speak like you do!\n\n"
                + "To stop ALISON from learning from you, use the optout command!";
    }

    @Override
    public String getShorthand() {
        return "i";
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