package youyihj.probezs.tree;

import crafttweaker.util.IEventHandler;
import crafttweaker.zenscript.expand.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.io.FileUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolJavaStaticField;
import stanhebben.zenscript.symbols.SymbolJavaStaticGetter;
import stanhebben.zenscript.symbols.SymbolJavaStaticMethod;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.type.natives.JavaMethod;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author youyihj
 */
public class ZenClassTree {
    private static ZenClassTree root;

    private final Map<String, ZenClassNode> classes = new LinkedHashMap<>();
    private final Map<Class<?>, ZenClassNode> javaMap = new HashMap<>();
    private final List<LazyZenClassNode> lazyZenClassNodes = new ArrayList<>();
    private final List<IZenDumpable> globals = new ArrayList<>();

    private final Set<Class<?>> blackList = new HashSet<>();
    private final ZenClassNode unknownClass = new ZenClassNode("unknown", this);

    public static ZenClassTree getRoot() {
        if (root == null) {
            root = new ZenClassTree();
            root.addBlockList(
                    ExpandAnyDict.class,
                    ExpandAnyArray.class,
                    ExpandBool.class,
                    ExpandByte.class,
                    ExpandByteArray.class,
                    ExpandFloat.class,
                    ExpandInt.class,
                    ExpandIntArray.class,
                    ExpandLong.class,
                    ExpandShort.class
            );
        }
        return root;
    }

    public ZenClassTree() {
        registerPrimitiveClass();
        classes.put("unknown", unknownClass);
    }

    public void putClass(Class<?> clazz) {
        if (blackList.contains(clazz)) return;
        ZenClass zenClass = clazz.getAnnotation(ZenClass.class);
        ZenExpansion zenExpansion = clazz.getAnnotation(ZenExpansion.class);
        String name;
        if (zenClass != null) {
            name = zenClass.value();
        } else if (zenExpansion != null) {
            name = zenExpansion.value();
        } else return;
        ZenClassNode classNode = classes.computeIfAbsent(name, it -> new ZenClassNode(it, this));
        if (zenClass != null) {
            javaMap.put(clazz, classNode);
            classNode.readExtendClasses(clazz);
        }
        classNode.readMembers(clazz, zenClass != null);
    }

    public LazyZenClassNode createLazyClassNode(Type type) {
        LazyZenClassNode lazyZenClassNode = new LazyZenClassNode(type, this);
        lazyZenClassNodes.add(lazyZenClassNode);
        return lazyZenClassNode;
    }

    public void fresh() {
        lazyZenClassNodes.forEach(LazyZenClassNode::fresh);
    }

    public void addBlockList(Class<?>... classes) {
        blackList.addAll(Arrays.asList(classes));
    }

    public ZenClassNode getZenClassNode(Type type) {
        try {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (!clazz.isArray()) {
                    return javaMap.getOrDefault(((Class<?>) type), unknownClass);
                } else {
                    return new ZenClassNode(getZenClassNode(clazz.getComponentType()).getName() + "[]", this);
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] arguments = parameterizedType.getActualTypeArguments();
                if (parameterizedType.getRawType() == List.class) {
                    return new ZenClassNode("[" + getZenClassNode(arguments[0]).getName() + "]", this);
                }
                if (parameterizedType.getRawType() == IEventHandler.class) {
                    return new ZenClassNode("function(" + getZenClassNode(arguments[0]).getName() + ")void", this);
                }
                if (parameterizedType.getRawType() == Map.class) {
                    return new ZenClassNode(getZenClassNode(arguments[1]).getName() + "[" + getZenClassNode(arguments[0]).getName() + "]", this);
                }
                return unknownClass;
            }
        } catch (Exception e) {
            return unknownClass;
        }
        return unknownClass;
    }

    public ZenClassNode getUnknownClass() {
        return unknownClass;
    }

    public void output() {
        IndentStringBuilder builder = new IndentStringBuilder();
        builder.append("#norun");
        builder.interLine();
        for (IZenDumpable global : globals) {
            global.toZenScript(builder);
            builder.nextLine();
        }
        builder.nextLine();
        for (ZenClassNode classNode : classes.values()) {
            classNode.toZenScript(builder);
            builder.interLine();
        }
        try {
            FileUtils.write(new File("scripts" + File.separator + ".d.zs"), builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readGlobals(Map<String, IZenSymbol> globalMap) {
        globalMap.forEach((name, symbol) -> {
            if (symbol instanceof SymbolJavaStaticField) {
                SymbolJavaStaticField javaStaticField = (SymbolJavaStaticField) symbol;
                Field field = ObfuscationReflectionHelper.getPrivateValue(SymbolJavaStaticField.class, javaStaticField, "field");
                globals.add(new ZenGlobalFieldNode(name, createLazyClassNode(field.getGenericType())));
            } else if (symbol instanceof SymbolJavaStaticGetter) {
                SymbolJavaStaticGetter javaStaticGetter = (SymbolJavaStaticGetter) symbol;
                IJavaMethod method = ObfuscationReflectionHelper.getPrivateValue(SymbolJavaStaticGetter.class, javaStaticGetter, "method");
                globals.add(new ZenGlobalFieldNode(name, createLazyClassNode(method.getReturnType().toJavaClass())));
            } else if (symbol instanceof SymbolJavaStaticMethod) {
                SymbolJavaStaticMethod javaStaticMethod = (SymbolJavaStaticMethod) symbol;
                IJavaMethod javaMethod = ObfuscationReflectionHelper.getPrivateValue(SymbolJavaStaticMethod.class, javaStaticMethod, "method");
                if (javaMethod instanceof JavaMethod) {
                    globals.add(ZenGlobalMethodNode.read(name, ((JavaMethod) javaMethod).getMethod(), this));
                }
            }
        });
    }

    private void registerPrimitiveClass() {
        ZenClassNode intNode = new ZenClassNode("int", this);
        ZenClassNode longNode = new ZenClassNode("long", this);
        ZenClassNode byteNode = new ZenClassNode("byte", this);
        ZenClassNode shortNode = new ZenClassNode("short", this);
        ZenClassNode booleanNode = new ZenClassNode("bool", this);
        ZenClassNode floatNode = new ZenClassNode("float", this);
        ZenClassNode doubleNode = new ZenClassNode("double", this);
        ZenClassNode stringNode = new ZenClassNode("string", this);
        ZenClassNode voidNode = new ZenClassNode("void", this);
        javaMap.put(int.class, intNode);
        javaMap.put(Integer.class, intNode);
        javaMap.put(long.class, longNode);
        javaMap.put(Long.class, longNode);
        javaMap.put(byte.class, byteNode);
        javaMap.put(Byte.class, byteNode);
        javaMap.put(short.class, shortNode);
        javaMap.put(Short.class, shortNode);
        javaMap.put(boolean.class, booleanNode);
        javaMap.put(Boolean.class, booleanNode);
        javaMap.put(float.class, floatNode);
        javaMap.put(Float.class, floatNode);
        javaMap.put(double.class, doubleNode);
        javaMap.put(Double.class, doubleNode);
        javaMap.put(void.class, voidNode);
        javaMap.put(String.class, stringNode);
        javaMap.put(Object.class, new ZenClassNode("Object", this));
    }
}
