package com.koudesuk.functionalstorage.block.config;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A commented key = value file in the config folder. The part of a key before the first
 * dot is its section. Every load rewrites the file, so keys added by an update show up
 * and bad values are replaced by the ones actually in use.
 */
public class ConfigFile {

    private final Path path;
    private final String header;
    private final List<Value<?>> values = new ArrayList<>();

    public ConfigFile(String name, String header) {
        this.path = FabricLoader.getInstance().getConfigDir().resolve(name + ".properties");
        this.header = header;
    }

    public IntValue defineInt(String key, String comment, int def, int min, int max) {
        IntValue value = new IntValue(key, comment, def, min, max);
        values.add(value);
        return value;
    }

    public DoubleValue defineDouble(String key, String comment, double def, double min, double max) {
        DoubleValue value = new DoubleValue(key, comment, def, min, max);
        values.add(value);
        return value;
    }

    public void load() {
        Properties file = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                file.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                FunctionalStorage.LOGGER.error("Could not read {}, using defaults", path, e);
            }
        }
        apply(file);
        save();
    }

    /**
     * Sets every value from its raw text; missing keys go back to their defaults.
     */
    public void apply(Map<?, ?> raw) {
        for (Value<?> value : values)
            value.set(raw.get(value.key));
    }

    public Map<String, String> snapshot() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Value<?> value : values)
            map.put(value.key, value.toString());
        return map;
    }

    public List<Value<?>> values() {
        return Collections.unmodifiableList(values);
    }

    public void save() {
        StringBuilder out = new StringBuilder();
        header.lines().forEach(line -> out.append("# ").append(line).append('\n'));
        String section = null;
        for (Value<?> value : values) {
            if (!value.section().equals(section)) {
                section = value.section();
                out.append("\n#################### ").append(section).append(" ####################\n");
            }
            out.append('\n');
            value.comment.lines().forEach(line -> out.append("# ").append(line).append('\n'));
            out.append("# ").append(value.rangeLine()).append('\n');
            out.append(value.key).append(" = ").append(value).append('\n');
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, out);
        } catch (IOException e) {
            FunctionalStorage.LOGGER.error("Could not write {}", path, e);
        }
    }

    public abstract static class Value<T extends Number & Comparable<T>> {
        private final String key;
        private final String comment;
        private final T def;
        private final T min;
        private final T max;
        protected T value;

        private Value(String key, String comment, T def, T min, T max) {
            this.key = key;
            this.comment = comment;
            this.def = def;
            this.min = min;
            this.max = max;
            this.value = def;
        }

        protected abstract T parse(String raw);

        public String key() {
            return key;
        }

        public String comment() {
            return comment;
        }

        /**
         * The part of the key before the first dot.
         */
        public String section() {
            return key.substring(0, key.indexOf('.'));
        }

        public String rangeLine() {
            return "Range: " + min + " ~ " + max + "    Default: " + def;
        }

        /**
         * Whether the text is a number inside the range, i.e. would be used as is.
         */
        public boolean accepts(String raw) {
            try {
                T parsed = parse(raw.trim());
                return parsed.compareTo(min) >= 0 && parsed.compareTo(max) <= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private void set(Object raw) {
            if (raw == null) {
                value = def;
                return;
            }
            try {
                T parsed = parse(raw.toString().trim());
                value = parsed.compareTo(min) < 0 ? min : parsed.compareTo(max) > 0 ? max : parsed;
                if (!value.equals(parsed))
                    FunctionalStorage.LOGGER.warn("Config {} = {} is outside {} ~ {}, using {}", key, raw, min, max,
                            value);
            } catch (NumberFormatException e) {
                FunctionalStorage.LOGGER.warn("Config {} = {} is not a valid number, using {}", key, raw, def);
                value = def;
            }
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public static final class IntValue extends Value<Integer> {
        private IntValue(String key, String comment, int def, int min, int max) {
            super(key, comment, def, min, max);
        }

        public int get() {
            return value;
        }

        @Override
        protected Integer parse(String raw) {
            return Integer.parseInt(raw);
        }
    }

    public static final class DoubleValue extends Value<Double> {
        private DoubleValue(String key, String comment, double def, double min, double max) {
            super(key, comment, def, min, max);
        }

        public double get() {
            return value;
        }

        @Override
        protected Double parse(String raw) {
            return Double.parseDouble(raw);
        }
    }
}
