package io.github.bluelhf.konfig.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public abstract class SnapshotProvider<S> {
    public abstract boolean isValid(Path path);
    public abstract S deserialise(BufferedInputStream stream) throws IOException;
    public abstract void serialise(BufferedOutputStream stream, S s) throws IOException;

    protected final boolean endsWith(Path path, String end) {
        if (path == null || path.getFileName() == null) return false;
        return path.getFileName().toString().endsWith(end);
    }
}
