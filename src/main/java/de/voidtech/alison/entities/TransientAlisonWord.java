package main.java.de.voidtech.alison.entities;

import java.io.Serializable;

public class TransientAlisonWord implements Serializable {

    private static final long serialVersionUID = -86154588887368562L;

    private final String word;

    private final String next;

    private int frequency;

    public TransientAlisonWord(String word, String next) {
        this.word = word;
        this.next = next;
        this.frequency = 1;
    }

    public void incrementCount() {
        this.frequency++;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public boolean isStopWord() {
        return !this.next.equals("StopWord");
    }

    public String getWord() {
        return this.word;
    }

    public String getNext() {
        return this.next;
    }
}
