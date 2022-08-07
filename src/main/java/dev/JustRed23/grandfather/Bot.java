package dev.JustRed23.grandfather;

import dev.JustRed23.stonebrick.cfg.ConfigField;
import dev.JustRed23.stonebrick.cfg.Configurable;

@Configurable
public class Bot {

    @ConfigField(defaultValue = "false")
    public static boolean enabled;

    @ConfigField(defaultValue = "true")
    public static boolean auto_update;

    @ConfigField(defaultValue = "Grandfather")
    public static String name;

    @ConfigField(defaultValue = "2911")
    public static String tag;

    @ConfigField(defaultValue = "g!")
    public static String prefix;

    @ConfigField(defaultValue = "https://github.com/JustRed23/Grandfather")
    public static String website_url;

    @ConfigField(defaultValue = "")
    public static String token;

    @ConfigField(defaultValue = "")
    public static String youtube_api_key;

    @ConfigField(defaultValue = "253219587787718658")
    public static String owner_id;

    @ConfigField(defaultValue = "826438912535691295")
    public static String guild_id;
}
