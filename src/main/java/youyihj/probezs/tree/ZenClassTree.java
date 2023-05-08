package youyihj.probezs.tree;

import crafttweaker.util.IEventHandler;
import crafttweaker.zenscript.expand.*;
import org.apache.commons.io.FileUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenExpansion;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author youyihj
 */
public class ZenClassTree {
    private static ZenClassTree root;

    private final Map<String, ZenClassNode> classes = new HashMap<>();
    private final Map<Class<?>, ZenClassNode> javaMap = new HashMap<>();
    private final List<LazyZenClassNode> lazyZenClassNodes = new ArrayList<>();

    private final Set<Class<?>> blackList = new HashSet<>();

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
                    return javaMap.get(((Class<?>) type));
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
                    return new ZenClassNode("crafttweaker.events.IEventHandler<" + getZenClassNode(arguments[0]).getName() + ">", this);
                }
                if (parameterizedType.getRawType() == Map.class) {
                    return new ZenClassNode(getZenClassNode(arguments[1]).getName() + "[" + getZenClassNode(arguments[0]).getName() + "]", this);
                }
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public void output() {
        IndentStringBuilder builder = new IndentStringBuilder();
        builder.append("#norun");
        builder.interLine();
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
    }
}
