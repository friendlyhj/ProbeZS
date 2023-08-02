package youyihj.probezs.bracket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import crafttweaker.zenscript.IBracketHandler;
import org.apache.commons.io.FileUtils;
import youyihj.probezs.docs.BracketReturnTypes;
import youyihj.probezs.tree.ZenClassTree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author youyihj
 */
public class ZenBracketTree {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Map<Class<?>, ZenBracketNode> nodes = new LinkedHashMap<>();
    private final ZenClassTree tree;

    public ZenBracketTree(ZenClassTree tree) {
        this.tree = tree;
    }

    public void addHandler(IBracketHandler bracketHandler) {
        Class<?> returnedClass = BracketReturnTypes.find(bracketHandler);
        if (returnedClass != null && !nodes.containsKey(returnedClass)) {
            ZenBracketNode bracketNode = new ZenBracketNode(tree.createLazyClassNode(returnedClass));
            nodes.put(returnedClass, bracketNode);
        }
    }

    public void addNode(Class<?> clazz, ZenBracketNode bracketNode) {
        nodes.put(clazz, bracketNode);
    }

    public void putContent(String prefix, Class<?> clazz, Stream<String> content) {
        nodes.get(clazz).addContent(content.map(it -> prefix + ":" + it).collect(Collectors.toList()));
    }

    public void output() {
        JsonArray brackets = new JsonArray();
        for (ZenBracketNode value : nodes.values()) {
            brackets.add(value.toJson());
        }
        try {
            FileUtils.write(new File("scripts" + File.separator + "generated" + File.separator + "brackets.json"), GSON.toJson(brackets), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
