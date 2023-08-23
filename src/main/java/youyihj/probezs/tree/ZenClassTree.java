package youyihj.probezs.tree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import crafttweaker.zenscript.expand.ExpandAnyArray;
import crafttweaker.zenscript.expand.ExpandAnyDict;
import crafttweaker.zenscript.expand.ExpandByteArray;
import crafttweaker.zenscript.expand.ExpandIntArray;
import org.apache.commons.io.FileUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.value.IntRange;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.tree.primitive.*;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.LoadingObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenClassTree {
    private static LoadingObject<ZenClassTree> root;
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(new TypeToken<Supplier<LazyZenClassNode.Result>>() {}.getType(), new LazyZenClassNode.Serializer())
            .registerTypeAdapter(LazyZenClassNode.class, new LazyZenClassNode.Serializer())
            .registerTypeAdapter(ZenClassNode.class, new ZenClassNode.Serializer())
            .registerTypeAdapter(ZenParameterNode.class, new ZenParameterNode.Serializer())
            .registerTypeAdapter(ZenOperatorNode.As.class, new ZenOperatorNode.AsSerializer())
            .create();

    private final Map<String, ZenClassNode> classes = new LinkedHashMap<>();
    private final Map<Class<?>, ZenClassNode> javaMap = new HashMap<>();
    private final List<LazyZenClassNode> lazyZenClassNodes = new ArrayList<>();

    private final Set<Class<?>> blackList = new HashSet<>();
    private final ZenClassNode anyClass = new ZenAnyNode(this);

    public static ZenClassTree getRoot() {
        if (root == null) {
            root = LoadingObject.of(new ZenClassTree());
            root.get().addBlockList(
                    ExpandAnyDict.class,
                    ExpandAnyArray.class,
                    ExpandByteArray.class,
                    ExpandIntArray.class
            );
        }
        return root.get();
    }

    public ZenClassTree() {
        registerPrimitiveClasses();
    }

    public void putClass(Class<?> clazz) {
        if (blackList.contains(clazz)) return;
        ZenClass zenClass = clazz.getAnnotation(ZenClass.class);
        ZenExpansion zenExpansion = clazz.getAnnotation(ZenExpansion.class);
        if (zenClass != null) {
            String name = zenClass.value();
            ZenClassNode classNode = classes.computeIfAbsent(name, it -> new ZenClassNode(it, this));
            javaMap.put(clazz, classNode);
            classNode.readExtendClasses(clazz);
            classNode.readMembers(clazz, true);
        }
        if (zenExpansion != null) {
            String name = zenExpansion.value();
            ZenClassNode classNode = classes.computeIfAbsent(name, it -> new ZenClassNode(it, this));
            classNode.readMembers(clazz, false);
        }
    }

    public LazyZenClassNode createLazyClassNode(Type type) {
        LazyZenClassNode lazyZenClassNode = new LazyZenClassNode(Objects.requireNonNull(type), this);
        lazyZenClassNodes.add(lazyZenClassNode);
        return lazyZenClassNode;
    }

    public void fresh() {
        lazyZenClassNodes.forEach(LazyZenClassNode::fresh);
    }

    public void addBlockList(Class<?>... classes) {
        blackList.addAll(Arrays.asList(classes));
    }

    public ZenClassNode getAnyClass() {
        return anyClass;
    }

    public Map<Class<?>, ZenClassNode> getJavaMap() {
        return javaMap;
    }

    public Map<String, ZenClassNode> getClasses() {
        return classes;
    }

    public void output() {
        for (ZenClassNode classNode : classes.values()) {
            String filePathWithoutExtension = "scripts" + File.separator + "generated" + File.separator + classNode.getName().replace('.', File.separatorChar);
            IndentStringBuilder builder = new IndentStringBuilder();
            classNode.toZenScript(builder);
            try {
                if (ProbeZSConfig.dumpDZS) {
                    FileUtils.write(new File(filePathWithoutExtension + ".dzs"), builder.toString(), StandardCharsets.UTF_8);
                }
                if (ProbeZSConfig.dumpJson) {
                    FileUtils.write(new File(filePathWithoutExtension + ".json"), GSON.toJson(classNode, ZenClassNode.class), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                ProbeZS.logger.error("Failed to output: {}" + classNode.getName(), e);
            }
        }
    }

    public void putGlobalInternalClass(Class<?> clazz) {
        if (!javaMap.containsKey(clazz)) {
            ZenClassNode classNode = classes.computeIfAbsent(clazz.getName(), it -> new ZenClassNode(it, this));
            javaMap.put(clazz, classNode);
            classNode.readMembers(clazz, true);
        }
    }

    private void registerPrimitiveClass(Class<?> javaClass, ZenClassNode node) {
        classes.put(node.getName(), node);
        javaMap.put(javaClass, node);
    }

    private void registerPrimitiveClasses() {
        ZenClassNode intNode = new ZenIntNode(this);
        ZenClassNode longNode = new ZenLongNode(this);
        ZenClassNode byteNode = new ZenByteNode(this);
        ZenClassNode shortNode = new ZenShortNode(this);
        ZenClassNode booleanNode = new ZenBoolNode(this);
        ZenClassNode floatNode = new ZenFloatNode(this);
        ZenClassNode doubleNode = new ZenDoubleNode(this);
        ZenClassNode stringNode = new ZenStringNode(this);
        ZenClassNode voidNode = new ZenVoidNode(this);
        registerPrimitiveClass(int.class, intNode);
        registerPrimitiveClass(Integer.class, intNode);
        registerPrimitiveClass(long.class, longNode);
        registerPrimitiveClass(Long.class, longNode);
        registerPrimitiveClass(byte.class, byteNode);
        registerPrimitiveClass(Byte.class, byteNode);
        registerPrimitiveClass(short.class, shortNode);
        registerPrimitiveClass(Short.class, shortNode);
        registerPrimitiveClass(boolean.class, booleanNode);
        registerPrimitiveClass(Boolean.class, booleanNode);
        registerPrimitiveClass(float.class, floatNode);
        registerPrimitiveClass(Float.class, floatNode);
        registerPrimitiveClass(double.class, doubleNode);
        registerPrimitiveClass(Double.class, doubleNode);
        javaMap.put(void.class, voidNode);
        registerPrimitiveClass(String.class, stringNode);
        registerPrimitiveClass(CharSequence.class, stringNode);
        registerPrimitiveClass(IntRange.class, new ZenIntRangeNode(this));
        javaMap.put(Object.class, anyClass);
    }
}
