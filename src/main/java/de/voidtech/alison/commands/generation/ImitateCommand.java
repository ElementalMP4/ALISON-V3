package main.java.de.voidtech.alison.commands.generation;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import main.java.de.voidtech.alison.service.AlisonService;
import main.java.de.voidtech.alison.service.PrivacyService;
import main.java.de.voidtech.alison.service.WebhookManager;
import main.java.de.voidtech.alison.util.ParsingUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;

@Command
public class ImitateCommand extends AbstractCommand {

    @Autowired
    private AlisonService textService;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private WebhookManager webhookManager;

    @Override
    public void execute(CommandContext context) {
        String ID;

        if (context.isSlashCommand()) {
            if (context.getEvent().getOption("user") == null) ID = context.getAuthor().getId();
            else ID = context.getEvent().getOption("user").getAsUser().getId();
        } else {
            if (context.getArgs().isEmpty()) ID = context.getAuthor().getId();
            else ID = context.getArgs().get(0).replaceAll("([^0-9a-zA-Z])", "");
        }

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
        return "Allows you to use the power of ALISON to imitate someone";
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

    @Override
    public SlashCommandOptions getSlashCommandOptions() {
        return new SlashCommandOptions(new OptionData(OptionType.USER, "user", "The user to imitate", false));
    }
}