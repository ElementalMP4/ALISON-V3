package main.java.de.voidtech.alison.util;

public class Stopwatch {
    private long timeStart;
    private long timeElapsed;

    public Stopwatch start() {
        this.timeStart = System.currentTimeMillis();
        return this;
    }

    public Stopwatch stop() {
        this.timeElapsed = System.currentTimeMillis() - this.timeStart;
        return this;
    }

    public long getTime() {
        return this.timeElapsed;
    }
}
