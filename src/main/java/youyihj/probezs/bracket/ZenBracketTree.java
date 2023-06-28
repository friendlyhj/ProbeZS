package youyihj.probezs.bracket;

import crafttweaker.zenscript.IBracketHandler;
import org.apache.commons.io.FileUtils;
import youyihj.probezs.docs.BracketReturnTypes;
import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.primitive.IPrimitiveType;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author youyihj
 */
public class ZenBracketTree {
    private final Map<Class<?>, ZenBracketNode> nodes = new LinkedHashMap<>();
    private int nextNodeId;
    private final ZenClassTree tree;

    public ZenBracketTree(ZenClassTree tree) {
        this.tree = tree;
    }

    public void addHandler(IBracketHandler bracketHandler) {
         Class<?> returnedClass = BracketReturnTypes.find(bracketHandler);
        if (returnedClass != null && !nodes.containsKey(returnedClass)) {
            ZenBracketNode bracketNode = new ZenBracketNode(tree.createLazyClassNode(returnedClass), nextNodeId++);
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
        IndentStringBuilder builder = new IndentStringBuilder();
        Set<ZenClassNode> imports = new TreeSet<>();
        nodes.values().forEach(it -> {
            it.fillImportMembers(imports);
        });
        for (ZenClassNode anImport : imports) {
            if (!(anImport instanceof IPrimitiveType)) {
                builder.append("import ").append(anImport.getName()).append(";").nextLine();
            }
        }
        builder.nextLine();
        nodes.values().forEach(it -> {
            it.toZenScript(builder);
            builder.interLine();
        });
        try {
            FileUtils.write(new File("scripts" + File.separator + "generated" + File.separator + "brackets.dzs"), builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
