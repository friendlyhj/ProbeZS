package youyihj.probezs.bracket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import youyihj.probezs.tree.LazyZenClassNode;
import youyihj.probezs.tree.ZenClassTree;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class BracketHandlerMirror {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LazyZenClassNode.class, new LazyZenClassNode.FullNameSerializer())
            .create();

    private final LazyZenClassNode type;
    private final String regex;
    private final Map<String, BracketHandlerEntryProperties> entries;

    public BracketHandlerMirror(LazyZenClassNode type, String regex, Map<String, BracketHandlerEntryProperties> entities) {
        this.type = type;
        this.regex = regex;
        this.entries = entities;
    }

    public LazyZenClassNode getType() {
        return type;
    }

    public String getRegex() {
        return regex;
    }

    public Map<String, BracketHandlerEntryProperties> getEntries() {
        return entries;
    }

    public static <T> Builder<T> builder(ZenClassTree classTree) {
        return new Builder<>(classTree);
    }

    public static class Builder<T> {
        private final ZenClassTree classTree;
        private Class<?> type;
        private String regex;
        private Collection<T> entries;
        private Function<T, String> idMapper;
        private BiConsumer<T, BracketHandlerEntryProperties> propertiesAdder;

        Builder(ZenClassTree classTree) {
            this.classTree = classTree;
        }

        public Builder<T> setType(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder<T> setRegex(String regex) {
            this.regex = regex;
            return this;
        }

        public Builder<T> setEntries(Collection<T> entries) {
            this.entries = entries;
            return this;
        }

        public Builder<T> setIdMapper(Function<T, String> idMapper) {
            this.idMapper = idMapper;
            return this;
        }

        public Builder<T> setPropertiesAdder(BiConsumer<T, BracketHandlerEntryProperties> propertiesAdder) {
            this.propertiesAdder = propertiesAdder;
            return this;
        }

        public BracketHandlerMirror build() {
            Map<String, BracketHandlerEntryProperties> entities = entries.stream()
                    .collect(Collectors.toMap(idMapper, it -> {
                        BracketHandlerEntryProperties properties = new BracketHandlerEntryProperties();
                        if (propertiesAdder != null) {
                            propertiesAdder.accept(it, properties);
                        }
                        return properties;
                    }));
            return new BracketHandlerMirror(classTree.createLazyClassNode(type), regex, entities);
        }
    }
}
