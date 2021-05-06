package io.github.bluelhf.konfig.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.*;
import java.nio.file.Path;

public class JsonProvider extends SnapshotProvider<JsonElement> {

    private final Gson gson;

    public JsonProvider(GsonBuilder builder) {
        this.gson = builder.create();
    }

    @Override
    public boolean isValid(Path path) {
        return endsWith(path, ".json");
    }

    @Override
    public JsonElement deserialise(BufferedInputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return gson.fromJson(reader, JsonElement.class);
        }
    }

    @Override
    public void serialise(BufferedOutputStream stream, JsonElement element) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            gson.toJson(element, writer);
        }
    }
}
