package dev.JustRed23.grandfather.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.JustRed23.grandfather.GFS;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;

import java.util.Arrays;
import java.util.List;

public final class LavalinkUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void addNodes(LavalinkClient client) {
        final List<String> content = GFS.nodesFile.getContent();
        if (content.isEmpty()) return;
        String json = String.join("", content);
        NodesFile file = GSON.fromJson(json, NodesFile.class);

        Arrays.stream(file.nodes).forEach(node ->
                client.addNode(new NodeOptions.Builder()
                        .setName(node.getName())
                        .setServerUri(node.getUrl())
                        .setPassword(node.getPassword())
                        .build())
        );
    }

    public static class NodesFile {
        private Node[] nodes;

        public static class Node {
            private String name;
            private String url;
            private String password;

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            public String getPassword() {
                return password;
            }
        }
    }
}
