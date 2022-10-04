package dev.JustRed23.grandfather.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {

    public static boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ignored) {
            return false;
        }
    }

    public static String linkText(String text, String url) {
        return "[" + text + "](" + url + ")";
    }
}
