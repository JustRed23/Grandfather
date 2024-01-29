package dev.JustRed23.grandfather.utils;

import java.net.MalformedURLException;
import java.net.URL;

public final class HttpUtils {

    public static boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
