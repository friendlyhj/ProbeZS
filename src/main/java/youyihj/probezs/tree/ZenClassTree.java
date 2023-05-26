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
import youyihj.probezs.tree.primitive.*;
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
    private final ZenClassNode anyClass = new ZenClassNode("any", this);

    public static ZenClassTree getRoot() {
        if (root == null) {
            root = new ZenClassTree();
            root.addBlockList(
                    ExpandAnyDict.class,
                    ExpandAnyArray.class,
                    ExpandByteArray.class,
                    ExpandIntArray.class
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
                    return javaMap.computeIfAbsent(((Class<?>) type), it -> {
                        for (Class<?> anInterface : it.getInterfaces()) {
                            ZenClassNode classNode = javaMap.get(anInterface);
                            if (classNode != null) return classNode;
                        }
                        for (Class<?> superClass = it.getSuperclass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
                            ZenClassNode classNode = javaMap.get(superClass);
                            if (classNode != null) return classNode;
                        }
                        return null;
                    });
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
                return getZenClassNode(parameterizedType.getRawType());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public ZenClassNode getAnyClass() {
        return anyClass;
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
                putGlobalInternalClass(field.getType());
                globals.add(new ZenGlobalFieldNode(name, createLazyClassNode(field.getGenericType())));
            } else if (symbol instanceof SymbolJavaStaticGetter) {
                SymbolJavaStaticGetter javaStaticGetter = (SymbolJavaStaticGetter) symbol;
                IJavaMethod method = ObfuscationReflectionHelper.getPrivateValue(SymbolJavaStaticGetter.class, javaStaticGetter, "method");
                putGlobalInternalClass(method.getReturnType().toJavaClass());
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

    private void putGlobalInternalClass(Class<?> clazz) {
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

    private void registerPrimitiveClass() {
        ZenClassNode intNode = new ZenIntNode(this);
        ZenClassNode longNode = new ZenLongNode(this);
        ZenClassNode byteNode = new ZenByteNode(this);
        ZenClassNode shortNode = new ZenShortNode(this);
        ZenClassNode booleanNode = new ZenBoolNode(this);
        ZenClassNode floatNode = new ZenFloatNode(this);
        ZenClassNode doubleNode = new ZenDoubleNode(this);
        ZenClassNode stringNode = new ZenStringNode(this);
        ZenClassNode voidNode = new ZenClassNode("void", this);
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
        javaMap.put(Object.class, anyClass);
    }
}
