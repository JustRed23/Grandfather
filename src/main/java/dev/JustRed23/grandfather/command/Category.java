package dev.JustRed23.grandfather.command;

public enum Category {
    ADMIN,
    FUN,
    GAMES,
    GENERAL,
    INTERNAL(false),
    MUSIC,
    NSFW,
    UTILITY;

    boolean show;
    Category(boolean show) {
        this.show = show;
    }

    Category() {
        this(true);
    }

    public boolean canShow() {
        return show;
    }
}
