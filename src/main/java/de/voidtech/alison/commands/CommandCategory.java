package main.java.de.voidtech.alison.commands;

public enum CommandCategory {
    TEXT_GENERATION("generation", ":robot:"),
    PRIVACY("privacy", ":lock:"),
    INFORMATION("information", ":books:"),
    MASTER("master", ":crown:"),
    MISC("misc", ":video_game:"),
    SENTIMENT_ANALYSIS("sentiment", ":heart:");

    private final String category;
    private final String iconName;

    CommandCategory(String category, String iconName) {
        this.category = category;
        this.iconName = iconName;
    }

    public String getCategory() {
        return category;
    }

    public String getIcon() {
        return iconName;
    }
}
