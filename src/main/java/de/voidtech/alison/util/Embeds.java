package main.java.de.voidtech.alison.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Embeds {

    public static final MessageEmbed TimedOutEmbed = new EmbedBuilder()
            .setTitle("Timed Out")
            .setDescription("Timed out waiting for a reply")
            .setColor(Color.GRAY)
            .build();

}
