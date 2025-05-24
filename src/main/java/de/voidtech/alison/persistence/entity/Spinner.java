package main.java.de.voidtech.alison.persistence.entity;

import javax.persistence.*;

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
    private String knockedOverBy;

    public Spinner(String serverID, String channelID, String userID) {
        this.serverID = serverID;
        this.channelID = channelID;
        this.userID = userID;
        this.spinnerStartTime = System.currentTimeMillis();
        this.isStillSpinning = true;
        this.spinnerEndTime = 0;
    }

    public Spinner() {
    }

    public String getChannelID() {
        return this.channelID;
    }

    public String getUserID() {
        return this.userID;
    }

    public void finishSpinner(String knockedOverBy) {
        this.spinnerEndTime = System.currentTimeMillis();
        this.isStillSpinning = false;
        this.knockedOverBy = knockedOverBy;
    }

    public long getSpinnerDuration() {
        return this.spinnerEndTime - this.spinnerStartTime;
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
}