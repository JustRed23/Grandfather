package dev.JustRed23.grandfather.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.GFS;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class LavalinkUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void addNodes(LavalinkClient client) {
        client.getNodes().forEach(client::removeNode);

        try {
            String response = HttpUtils.sendRequest("https://lavalink-list.ajieblogs.eu.org/All");
            Node[] nodes = GSON.fromJson(response, Node[].class);
            addNodes(nodes, client);
        } catch (Exception e) {
            App.LOGGER.error("Failed to fetch Lavalink nodes", e);
        }

        final List<String> content = GFS.nodesFile.getContent();
        if (content.isEmpty()) return;
        String json = String.join("", content);
        Node[] nodes = GSON.fromJson(json, Node[].class);
        addNodes(nodes, client);
    }

    private static void addNodes(Node[] nodes, LavalinkClient client) {
        if (nodes == null) return;
        List<Node> validNodes = Arrays.stream(nodes).filter(Node::isValid).toList();
        validNodes.stream().map(Node::toOptions).forEach(client::addNode);
    }

    public static class Node {
        private String uniqueId;
        private String identifier;
        private String host;
        private int port;
        private String password;
        private boolean secure;
        private String version;

        public boolean isValid() {
            return (version == null || version.equals("v4")) && HttpUtils.isValid(host);
        }

        public NodeOptions toOptions() {
            return new NodeOptions.Builder()
                    .setName(identifier)
                    .setServerUri(URI.create((secure ? "https" : "http") + "://" + host + ":" + port))
                    .setPassword(password)
                    .build();
        }
    }
}
