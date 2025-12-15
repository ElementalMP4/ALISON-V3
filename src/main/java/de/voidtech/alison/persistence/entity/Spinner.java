package main.java.de.voidtech.alison.persistence.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spinners")
public class Spinner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String serverID;

    @Column
    private String channelID;

    @Column
    private String userID;

    @Column
    private long spinnerStartTime;

    @Column
    private long spinnerEndTime;

    @Column
    private boolean isStillSpinning;

    @Column
    private long spinnerDuration;

    @Column
    private String knockedOverBy;

    public Spinner(String serverID, String channelID, String userID) {
        this.serverID = serverID;
        this.channelID = channelID;
        this.userID = userID;
        this.spinnerStartTime = System.currentTimeMillis();
        this.isStillSpinning = true;
        this.spinnerEndTime = 0;
        this.spinnerDuration = 0;
    }

    public Spinner() {
    }

    public void updateDuration() {
        this.spinnerDuration = System.currentTimeMillis() - this.spinnerStartTime;
    }

    public String getChannelID() {
        return this.channelID;
    }

    public String getUserID() {
        return this.userID;
    }

    public void finishSpinner(String knockedOverBy) {
        updateDuration();
        this.spinnerEndTime = System.currentTimeMillis();
        this.isStillSpinning = false;
        this.knockedOverBy = knockedOverBy;
    }

    public long getSpinnerDuration() {
        return this.spinnerDuration;
    }

    public long getSpinnerDurationSeconds() {
        return (long) Math.ceil(getSpinnerDuration() / 1000F);
    }

    public boolean isStillSpinning() {
        return this.isStillSpinning;
    }

    public String getKnockedOverBy() {
        return this.knockedOverBy;
    }

    public String durationAsText() {
        long duration = getSpinnerDurationSeconds();
        long days = duration / (24 * 3600);
        duration = duration % (24 * 3600);

        long hours = duration / 3600;
        duration %= 3600;

        long minutes = duration / 60;
        duration %= 60;

        long seconds = duration;
        List<String> output = new ArrayList<>();

        if (days > 0) output.add(days + " days");
        if (hours > 0) output.add(hours + " hours");
        if (minutes > 0) output.add(minutes + " minutes");
        output.add(seconds + " seconds");
        return String.join(", ", output);
    }

}