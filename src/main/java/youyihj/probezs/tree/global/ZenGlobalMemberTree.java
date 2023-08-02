package youyihj.probezs.tree.global;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import crafttweaker.mc1120.util.CraftTweakerHacks;
import org.apache.commons.io.FileUtils;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolJavaStaticField;
import stanhebben.zenscript.symbols.SymbolJavaStaticGetter;
import stanhebben.zenscript.symbols.SymbolJavaStaticMethod;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.type.natives.JavaMethod;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.tree.IZenDumpable;
import youyihj.probezs.tree.ZenClassNode;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.primitive.IPrimitiveType;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author youyihj
 */
public class ZenGlobalMemberTree {
    private final ZenClassTree tree;
    private final Set<ZenGlobalFieldNode> fields = new TreeSet<>();
    private final Set<ZenGlobalMethodNode> members = new TreeSet<>();

    public ZenGlobalMemberTree(ZenClassTree tree) {
        this.tree = tree;
    }

    public void readGlobals(Map<String, IZenSymbol> globalMap) {
        globalMap.forEach((name, symbol) -> {
            if (symbol instanceof SymbolJavaStaticField) {
                SymbolJavaStaticField javaStaticField = (SymbolJavaStaticField) symbol;
                Field field = CraftTweakerHacks.getPrivateObject(javaStaticField, "field");
                tree.putGlobalInternalClass(field.getType());
                fields.add(new ZenGlobalFieldNode(name, tree.createLazyClassNode(field.getGenericType())));
            } else if (symbol instanceof SymbolJavaStaticGetter) {
                SymbolJavaStaticGetter javaStaticGetter = (SymbolJavaStaticGetter) symbol;
                IJavaMethod method = CraftTweakerHacks.getPrivateObject(javaStaticGetter, "method");
                tree.putGlobalInternalClass(method.getReturnType().toJavaClass());
                fields.add(new ZenGlobalFieldNode(name, tree.createLazyClassNode(method.getReturnType().toJavaClass())));
            } else if (symbol instanceof SymbolJavaStaticMethod) {
                SymbolJavaStaticMethod javaStaticMethod = (SymbolJavaStaticMethod) symbol;
                IJavaMethod javaMethod = CraftTweakerHacks.getPrivateObject(javaStaticMethod, "method");
                if (javaMethod instanceof JavaMethod) {
                    members.add(ZenGlobalMethodNode.read(name, ((JavaMethod) javaMethod).getMethod(), tree));
                }
            }
        });
    }

    public Set<ZenClassNode> getImportMembers() {
        Set<ZenClassNode> imports = new TreeSet<ZenClassNode>() {
            @Override
            public boolean add(ZenClassNode node) {
                if (node instanceof IPrimitiveType) {
                    return false;
                } else {
                    return super.add(node);
                }
            }
        };
        for (ZenGlobalFieldNode field : fields) {
            field.fillImportMembers(imports);
        }
        for (ZenGlobalMethodNode member : members) {
            member.fillImportMembers(imports);
        }
        return imports;
    }

    public void output() {
        outputDZS();
        outputJson();
    }

    private void outputJson() {
        JsonObject json = new JsonObject();
        JsonArray imports = new JsonArray();
        for (ZenClassNode importMember : getImportMembers()) {
            imports.add(new JsonPrimitive(importMember.getName()));
        }
        json.add("imports", imports);
        json.add("fields", ZenClassTree.GSON.toJsonTree(fields, new TypeToken<Set<ZenGlobalFieldNode>>() {}.getType()));
        json.add("members", ZenClassTree.GSON.toJsonTree(members, new TypeToken<Set<ZenGlobalMethodNode>>() {}.getType()));
        try {
            FileUtils.write(new File("scripts" + File.separator + "generated" + File.separator + "globals.json"), ZenClassTree.GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ProbeZS.logger.error("Failed output globals dzs", e);
        }
    }

    private void outputDZS() {
        IndentStringBuilder sb = new IndentStringBuilder();
        for (ZenClassNode anImport : getImportMembers()) {
            sb.append("import ").append(anImport.getName()).append(";").nextLine();
        }
        sb.nextLine();
        for (IZenDumpable field : fields) {
            field.toZenScript(sb);
            sb.nextLine();
        }
        sb.nextLine();
        for (ZenGlobalMethodNode member : members) {
            member.toZenScript(sb);
            sb.nextLine();
        }
        try {
            FileUtils.write(new File("scripts" + File.separator + "generated" + File.separator + "globals.dzs"), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ProbeZS.logger.error("Failed output globals dzs", e);
        }
    }
}
