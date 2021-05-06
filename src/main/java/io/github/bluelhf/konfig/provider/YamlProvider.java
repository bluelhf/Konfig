package io.github.bluelhf.konfig.provider;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class YamlProvider extends SnapshotProvider<Map<String, Object>> {

    private final Yaml yaml;

    public YamlProvider(Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public boolean isValid(Path path) {
        return endsWith(path, ".yml");
    }

    @Override
    public Map<String, Object> deserialise(BufferedInputStream stream) throws IOException {
        return yaml.load(stream);
    }

    @Override
    public void serialise(BufferedOutputStream stream, Map<String, Object> stringObjectMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            yaml.dump(stringObjectMap, writer);
        }
    }
}
