package dev.JustRed23.grandfather;

import dev.JustRed23.abcm.ConfigField;
import dev.JustRed23.abcm.Configurable;

@Configurable
public class Bot {

    public static final long START_TIME = System.currentTimeMillis();

    @ConfigField(defaultValue = "false")
    public static boolean enabled;

    @ConfigField(defaultValue = "true")
    public static boolean auto_update;

    @ConfigField(defaultValue = "Grandfather")
    public static String name;

    @ConfigField(defaultValue = "2911")
    public static String tag;

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
