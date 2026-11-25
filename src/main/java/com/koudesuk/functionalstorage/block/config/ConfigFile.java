package com.koudesuk.functionalstorage.block.config;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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

    public IdListValue defineIdList(String key, String comment) {
        IdListValue value = new IdListValue(key, comment);
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

    public abstract static class Value<T> {
        private final String key;
        private final String comment;
        protected final T def;
        protected T value;

        private Value(String key, String comment, T def) {
            this.key = key;
            this.comment = comment;
            this.def = def;
            this.value = def;
        }

        /**
         * Reads the text, throwing IllegalArgumentException when it is not a value at all.
         */
        protected abstract T parse(String raw);

        /**
         * The value actually used for a parsed one, e.g. clamped into its range.
         */
        protected T fit(T parsed) {
            return parsed;
        }

        protected String format(T value) {
            return value.toString();
        }

        public abstract String rangeLine();

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

        /**
         * Whether the text would be used as is: valid, and for numbers inside the range.
         */
        public boolean accepts(String raw) {
            try {
                T parsed = parse(raw.trim());
                return fit(parsed).equals(parsed);
            } catch (IllegalArgumentException e) {
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
                value = fit(parsed);
                if (!value.equals(parsed))
                    FunctionalStorage.LOGGER.warn("Config {} = {} is outside the {}, using {}", key, raw, rangeLine(),
                            format(value));
            } catch (IllegalArgumentException e) {
                FunctionalStorage.LOGGER.warn("Config {} = {} is not valid, using {}", key, raw, format(def));
                value = def;
            }
        }

        @Override
        public String toString() {
            return format(value);
        }
    }

    public abstract static class NumberValue<T extends Number & Comparable<T>> extends Value<T> {
        private final T min;
        private final T max;

        private NumberValue(String key, String comment, T def, T min, T max) {
            super(key, comment, def);
            this.min = min;
            this.max = max;
        }

        @Override
        protected T fit(T parsed) {
            return parsed.compareTo(min) < 0 ? min : parsed.compareTo(max) > 0 ? max : parsed;
        }

        @Override
        public String rangeLine() {
            return "Range: " + min + " ~ " + max + "    Default: " + def;
        }
    }

    /**
     * Registry ids separated by commas, like "minecraft:stone, minecraft:dirt"; a missing
     * namespace means minecraft. Only the form is checked: other mods may not have registered
     * their content yet when the file is loaded.
     */
    public static final class IdListValue extends Value<Set<ResourceLocation>> {
        private IdListValue(String key, String comment) {
            super(key, comment, Set.of());
        }

        public boolean contains(ResourceLocation id) {
            return value.contains(id);
        }

        @Override
        protected Set<ResourceLocation> parse(String raw) {
            Set<ResourceLocation> ids = new LinkedHashSet<>();
            for (String part : raw.split(",")) {
                String text = part.trim();
                if (text.isEmpty())
                    continue;
                ResourceLocation id = ResourceLocation.tryParse(text);
                if (id == null)
                    throw new IllegalArgumentException("Not an id: " + text);
                ids.add(id);
            }
            return ids;
        }

        @Override
        protected String format(Set<ResourceLocation> ids) {
            return ids.stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
        }

        @Override
        public String rangeLine() {
            return "Comma separated ids, like minecraft:stone    Default: none";
        }
    }

    public static final class IntValue extends NumberValue<Integer> {
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

    public static final class DoubleValue extends NumberValue<Double> {
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
