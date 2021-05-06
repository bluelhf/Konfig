package io.github.bluelhf.konfig;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.bluelhf.konfig.provider.JsonProvider;
import io.github.bluelhf.konfig.provider.SnapshotProvider;
import io.github.bluelhf.konfig.provider.TomlProvider;
import io.github.bluelhf.konfig.provider.YamlProvider;
import io.github.bluelhf.konfig.util.FileMonitor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

public class Konfig<S> implements AutoCloseable {
    private final Path path;
    private final Consumer<Konfig<S>> changeTask;
    private final SnapshotProvider<S> provider;

    private S lastSnapshot;
    private FileMonitor monitor;

    Konfig(Path path, Consumer<Konfig<S>> changeTask, SnapshotProvider<S> provider) {
        this.path = path;
        this.changeTask = changeTask;
        this.provider = provider;

        if (!provider.isValid(path))
            throw new IllegalArgumentException(provider.getClass().getSimpleName() + " cannot parse " + path.getFileName());
        init();
    }

    public static <T> KonfigBuilder<T> builder(SnapshotProvider<T> provider) {
        return new KonfigBuilder<>(provider);
    }

    void init() {
        monitor = new FileMonitor(path.toFile(), Duration.ofMillis(5000), (file, change) -> {
            changeTask.accept(this);
        });
        monitor.start();
    }

    public Konfig<S> create() throws IOException {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        return this;
    }

    public S load(OpenOption... options) throws IOException {
        create();
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path, options))) {
            lastSnapshot = provider.deserialise(stream);
        }

        return lastSnapshot;
    }

    /**
     * Loads the configuration from the given {@link InputStream} and saves it into the target path.
     * */
    public S getFrom(InputStream stream) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(stream)) {
            lastSnapshot = provider.deserialise(in);
        }

        save();
        return lastSnapshot;
    }

    public S tryGetFrom(InputStream stream) {
        try {
            getFrom(stream);
        } catch (Throwable ignored) {
        }

        return lastSnapshot;
    }

    public S tryLoad(OpenOption... options) {
        try {
            load(options);
        } catch (Throwable ignored) {
        }

        return lastSnapshot;
    }

    public Konfig<S> trySave(OpenOption... options) {
        try {
            save(options);
        } catch (Throwable ignored) {
        }

        return this;
    }

    public S get() {
        return lastSnapshot;
    }

    public Konfig<S> save(OpenOption... options) throws IOException {
        try (BufferedOutputStream stream = new BufferedOutputStream(Files.newOutputStream(path, options))) {
            provider.serialise(stream, lastSnapshot);
        }

        return this;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws Exception {
        monitor.close();
        save();
    }

    /**
     * Basic utility method for reading Tom's Obvious, Minimal Language files
     * @return A Konfig object with the following properties:<br/>
     *   - A basic {@link TomlProvider} with a default TomlWriter<br/>
     *   - The given {@link Path}<br/>
     *   - A change listener that attempts to reload the Konfig when changes are detected.
     * */
    public static Konfig<Toml> forToml(Path path) {
        return Konfig.builder(new TomlProvider(new TomlWriter.Builder()))
                .at(path)
                .change(Konfig::tryLoad)
                .build();
    }

    /**
     * Basic utility method for reading YAML Ain't No Markup Language files
     * @return A Konfig object with the following properties:<br/>
     *   - A basic {@link YamlProvider} with a default Yaml<br/>
     *   - The given {@link Path}<br/>
     *   - A change listener that attempts to reload the Konfig when changes are detected.
     * */
    public static Konfig<Map<String, Object>> forYaml(Path path) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setExplicitStart(false);
        options.setExplicitEnd(false);
        return Konfig.builder(new YamlProvider(new Yaml(options)))
                .at(path)
                .change(Konfig::tryLoad)
                .build();
    }

    /**
     * Basic utility method for reading JavaScript Object Notation files
     * @return A Konfig object with the following properties:<br/>
     *   - A basic {@link JsonProvider} with a default lenient Gson<br/>
     *   - The given {@link Path}<br/>
     *   - A change listener that attempts to reload the Konfig when changes are detected.
     * */
    public static Konfig<JsonElement> forJson(Path path) {
        return Konfig.builder(new JsonProvider(new GsonBuilder().setLenient()))
                .at(path)
                .change(Konfig::tryLoad)
                .build();
    }
}
