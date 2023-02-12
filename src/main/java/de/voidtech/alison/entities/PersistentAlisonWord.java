package main.java.de.voidtech.alison.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "alison_words", indexes = @Index(columnList = "word", name = "idx_alison"))
public class PersistentAlisonWord
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String collection;

    @Column
    @Type(type = "org.hibernate.type.TextType")
    private String word;

    @Column
    @Type(type = "org.hibernate.type.TextType")
    private String next;

    @Deprecated
    PersistentAlisonWord() {
    }

    public PersistentAlisonWord(String collection, String word, String next) {
        this.collection = collection;
        this.word = word;
        this.next = next;
    }

    public String getCollection() {
        return this.collection;
    }

    public String getWord() {
        return this.word;
    }

    public String getNext() {
        return this.next;
    }

    public boolean isStopWord() {
        return this.next.equals("StopWord");
    }
}