package io.github.bluelhf.konfig;

import io.github.bluelhf.konfig.provider.SnapshotProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class KonfigBuilder<S> {
    private Consumer<Konfig<S>> changeTask;
    private Path path;
    private SnapshotProvider<S> provider;

    public KonfigBuilder(SnapshotProvider<S> provider) {
        this.provider = provider;
    }

    public KonfigBuilder<S> at(Path path) {
        this.path = path;
        return this;
    }

    public KonfigBuilder<S> at(File file) {
        this.path = file.toPath();
        return this;
    }

    public KonfigBuilder<S> change(Consumer<Konfig<S>> konfigConsumer) {
        this.changeTask = konfigConsumer;
        return this;
    }

    public Konfig<S> build() {
        return new Konfig<S>(path, changeTask, provider);
    }
}
