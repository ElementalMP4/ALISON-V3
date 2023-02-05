package main.java.de.voidtech.alison.entities;

import javax.persistence.*;

@Entity
@Table(name = "ignored_channel")
public class IgnoredChannel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String channelID;

    @Column
    private String guildID;

    @Deprecated
    IgnoredChannel() {
    }

    public IgnoredChannel(String channel, String guild)
    {
        this.channelID = channel;
        this.guildID = guild;
    }

    public String getChannel() {
        return this.channelID;
    }
}