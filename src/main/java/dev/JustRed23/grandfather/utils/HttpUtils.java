package dev.JustRed23.grandfather.utils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class HttpUtils {

    public static boolean isUrl(String url) {
        try {
            URL _ = URI.create(url).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValid(String host) {
        try {
            var _ = InetAddress.getByName(host);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String sendRequest(String endpoint) {
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .build();

            return client
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send HTTP request", e);
        }
    }
}
