package main.java.de.voidtech.alison.commands;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

public record SlashCommandOptions (List<OptionData> options, List<SubcommandData> subCommands) {

    public SlashCommandOptions() {
        this(List.of(), List.of());
    }

    public SlashCommandOptions(OptionData ...options) {
        this(List.of(options), List.of());
    }

    public SlashCommandOptions(SubcommandData ...cmd) {
        this(List.of(), List.of(cmd));
    }

}
