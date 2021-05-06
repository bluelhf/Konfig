package io.github.bluelhf.konfig.provider;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class TomlProvider extends SnapshotProvider<Toml> {
    private final TomlWriter writer;

    public TomlProvider(TomlWriter.Builder tomlBuilder) {
        writer = tomlBuilder.build();
    }

    @Override
    public boolean isValid(Path path) {
        return endsWith(path, ".toml");
    }

    @Override
    public Toml deserialise(BufferedInputStream stream) {
        return new Toml().read(stream);
    }

    @Override
    public void serialise(BufferedOutputStream stream, Toml toml) throws IOException {
        writer.write(toml, stream);
    }
}
