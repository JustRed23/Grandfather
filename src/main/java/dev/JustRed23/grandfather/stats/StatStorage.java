package dev.JustRed23.grandfather.stats;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class StatStorage {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final File file;
    public JsonObject data;

    public StatStorage(@Nullable File parentDir, String fileName) {
        if (parentDir != null && !parentDir.exists())
            if (!parentDir.mkdirs())
                throw new RuntimeException("Could not create parent directory for file: " + parentDir.getAbsolutePath() + "! Please check the file system permissions.");

        if (!fileName.endsWith(".json"))
            fileName = fileName.concat(".json");

        file = new File(parentDir, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                data = new JsonObject();
            } catch (Exception e) {
                throw new RuntimeException("Could not create file: " + file.getAbsolutePath() + "! Please check the file system permissions.", e);
            }
        } else {
            try (FileReader reader = new FileReader(file)) {
                data = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file: " + file.getAbsolutePath() + "! Please check the file system permissions.", e);
            }
        }
    }

    public StatStorage(String fileName) {
        this(null, fileName);
    }

    public void save(JsonObject data) {
        this.data = data;
        save();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save file: " + file.getAbsolutePath() + "! Please check the file system permissions.", e);
        }
    }
}
