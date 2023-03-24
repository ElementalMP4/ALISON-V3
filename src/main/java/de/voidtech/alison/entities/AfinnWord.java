package main.java.de.voidtech.alison.entities;

public class AfinnWord
{
    private String word;

    private int score;

    public AfinnWord(String word, int score) {
        this.word = word;
        this.score = score;
    }

    public String getWord() {
        return this.word;
    }

    public int getScore() {
        return this.score;
    }
}