package main.java.de.voidtech.alison.entities;

public class TransientClaireWord {
    private final String word;

    private final String next;

    public TransientClaireWord(String word, String next) {
        this.word = word;
        this.next = next;
    }

    public boolean isNotStopWord() {
        return !this.next.equals("StopWord");
    }

    public String getWord() {
        return this.word.replaceAll("/@/", "'");
    }

    public String getNext() {
        return this.next;
    }
}