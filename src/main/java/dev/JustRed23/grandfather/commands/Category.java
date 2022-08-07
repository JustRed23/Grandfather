package dev.JustRed23.grandfather.commands;

public enum Category {
    ADMIN,
    GENERAL,
    GAMES,
    MODERATION,
    UTILITY,
    MUSIC,
    FUN,
    NSFW,
    INTERNAL(false);

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
