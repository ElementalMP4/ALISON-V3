package main.java.de.voidtech.alison.entities;

public class ClaireWord {
    private final String word;

    private final String next;

    public ClaireWord(String word, String next) {
        this.word = word;
        this.next = next;
    }

    public boolean isNotStopWord() {
        return !this.next.equals("StopWord");
    }

    public String getWord() {
        return this.word;
    }

    public String getNext() {
        return this.next;
    }
}