package youyihj.probezs.tree.global;

import crafttweaker.mc1120.util.CraftTweakerHacks;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolJavaStaticField;
import stanhebben.zenscript.symbols.SymbolJavaStaticGetter;
import stanhebben.zenscript.symbols.SymbolJavaStaticMethod;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.type.natives.JavaMethod;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.tree.IZenDumpable;
import youyihj.probezs.tree.TypeNameContext;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.util.FileUtils;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
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
                fields.add(new ZenGlobalFieldNode(name, tree.createJavaTypeMirror(field.getGenericType())));
            } else if (symbol instanceof SymbolJavaStaticGetter) {
                SymbolJavaStaticGetter javaStaticGetter = (SymbolJavaStaticGetter) symbol;
                IJavaMethod method = CraftTweakerHacks.getPrivateObject(javaStaticGetter, "method");
                tree.putGlobalInternalClass(method.getReturnType().toJavaClass());
                fields.add(new ZenGlobalFieldNode(name, tree.createJavaTypeMirror(method.getReturnType().toJavaClass())));
            } else if (symbol instanceof SymbolJavaStaticMethod) {
                SymbolJavaStaticMethod javaStaticMethod = (SymbolJavaStaticMethod) symbol;
                IJavaMethod javaMethod = CraftTweakerHacks.getPrivateObject(javaStaticMethod, "method");
                if (javaMethod instanceof JavaMethod) {
                    members.add(ZenGlobalMethodNode.read(name, ProbeZS.getMemberFactory().reflect(((JavaMethod) javaMethod).getMethod()), tree));
                }
            }
        });
    }

    public TypeNameContext getTypeNameContext() {
        TypeNameContext context = new TypeNameContext(null);
        for (ZenGlobalFieldNode field : fields) {
            field.setMentionedTypes(context);
        }
        for (ZenGlobalMethodNode member : members) {
            member.setMentionedTypes(context);
        }
        return context;
    }

    public void output(Path dzsPath) {
        if (ProbeZSConfig.dumpDZS) {
            outputDZS(dzsPath);
        }
    }

    private void outputDZS(Path dzsPath) {
        IndentStringBuilder sb = new IndentStringBuilder();
        TypeNameContext context = getTypeNameContext();
        context.toZenScript(sb, context);
        sb.interLine();
        for (IZenDumpable field : fields) {
            field.toZenScript(sb, context);
            sb.nextLine();
        }
        sb.nextLine();
        for (ZenGlobalMethodNode member : members) {
            member.toZenScript(sb, context);
            sb.nextLine();
        }
        try {
            FileUtils.createFile(dzsPath.resolve("globals.dzs"), sb.toString());
        } catch (IOException e) {
            ProbeZS.logger.error("Failed output globals dzs", e);
        }
    }
}
