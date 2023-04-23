package main.java.de.voidtech.alison.persistence.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "claire_pairs", indexes = @Index(columnList = "message", name = "idx_claire"))
public class PersistentClairePair
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @Type(type = "org.hibernate.type.TextType")
    private String message;

    @Column
    @Type(type = "org.hibernate.type.TextType")
    private String reply;

    @Deprecated
    PersistentClairePair() {
    }

    public PersistentClairePair(String message, String reply) {
        this.message = message;
        this.reply = reply;
    }

    public String getMessage() {
        return this.message;
    }

    public String getReply() {
        return this.reply;
    }
}