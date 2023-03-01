package main.java.de.voidtech.alison.entities;

import javax.persistence.*;

@Entity
@Table(name = "user_metadata")
public class UserMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String languageChoice;

    @Column
    private String userID;

    @Deprecated
    public UserMetadata() {
    }

    public UserMetadata(String userID) {
        this.userID = userID;
    }

    public String getLanguageOption() {
        return this.languageChoice;
    }

    public void setLanguageChoice(String language) {
        this.languageChoice = language;
    }

}
