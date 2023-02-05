package main.java.de.voidtech.alison.entities;

import javax.persistence.*;

@Entity
@Table(name = "ignored_user")
public class IgnoredUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String userID;

    @Deprecated
    IgnoredUser() {
    }

    public IgnoredUser(String user)
    {
        this.userID = user;
    }
}