package dev.JustRed23.grandfather.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class HttpUtils {

    public static boolean isUrl(String url) {
        try {
            URL _ = URI.create(url).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }
}
