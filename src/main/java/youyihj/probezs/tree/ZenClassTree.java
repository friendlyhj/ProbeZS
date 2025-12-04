package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.value.IntRange;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.docs.ParameterNameMappings;
import youyihj.probezs.tree.primitive.*;
import youyihj.probezs.util.FileUtils;
import youyihj.probezs.util.IndentStringBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/**
 * @author youyihj
 */
public class ZenClassTree {
    private final Map<String, ZenClassNode> classes = new LinkedHashMap<>();
    private final Map<String, ZenExpandClassNode> builtinTypeExpansions = new LinkedHashMap<>();
    private final Map<String, ZenClassNode> javaMap = new HashMap<>();
    private final List<JavaTypeMirror> javaTypeMirrors = new ArrayList<>();
    private final ParameterNameMappings mappings = new ParameterNameMappings();

    private final Set<Class<?>> blackList = new HashSet<>();
    private final ZenClassNode anyClass = new ZenAnyNode(this);

    public ZenClassTree(Collection<Class<?>> zenClasses) {
        registerPrimitiveClasses();
        zenClasses.forEach(this::putClass);
    }

    public void putClass(Class<?> clazz) {
        try {
            if (blackList.contains(clazz)) return;
            ZenClass zenClass = clazz.getAnnotation(ZenClass.class);
            ZenExpansion zenExpansion = clazz.getAnnotation(ZenExpansion.class);
            if (zenClass != null) {
                String name = zenClass.value();
                if (name.isEmpty()) {
                    name = clazz.getName();
                }
                ZenClassNode classNode = classes.computeIfAbsent(name, it -> new ZenClassNode(it, this));
                javaMap.put(clazz.getName(), classNode);
                classNode.readExtendClasses(clazz);
                classNode.readMembers(clazz, true);
            }
            if (zenExpansion != null) {
                String name = zenExpansion.value();
                if (name.isEmpty()) {
                    return;
                }
                if (name.contains("[")) {
                    readBuiltinTypeExpansion(name, clazz);
                    return;
                }
                ZenClassNode classNode = classes.computeIfAbsent(name, it -> new ZenClassNode(it, this));
                if (classNode instanceof IBuiltinType) {
                    readBuiltinTypeExpansion(name, clazz);
                } else {
                    classNode.readMembers(clazz, false);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get members of " + clazz.getName() + ", try setting MemberCollector to ASM in config?", e);
        }
    }

    public JavaTypeMirror createJavaTypeMirror(Type type) {
        JavaTypeMirror javaTypeMirror = new JavaTypeMirror(Objects.requireNonNull(type), this);
        javaTypeMirrors.add(javaTypeMirror);
        return javaTypeMirror;
    }

    public void fresh() {
        javaTypeMirrors.forEach(JavaTypeMirror::fresh);
    }

    public void addBlackList(Class<?>... classes) {
        blackList.addAll(Arrays.asList(classes));
    }

    public ZenClassNode getAnyClass() {
        return anyClass;
    }

    public Map<String, ZenClassNode> getJavaMap() {
        return javaMap;
    }

    public Map<String, ZenClassNode> getClasses() {
        return classes;
    }

    public ParameterNameMappings getMappings() {
        return mappings;
    }

    public void output(Path dzsPath) {
        for (ZenClassNode classNode : classes.values()) {
            String filePath = classNode.getName().replace('.', '/');
            if (classNode instanceof IBuiltinType) continue;
            try {
                if (ProbeZSConfig.dumpDZS) {
                    IndentStringBuilder builder = new IndentStringBuilder();
                    classNode.toZenScript(builder, classNode.getTypeNameContext());
                    FileUtils.createFile(dzsPath.resolve(filePath + ".dzs"),
                            builder.toString()
                    );
                }
            } catch (IOException e) {
                ProbeZS.logger.error("Failed to output: {} {}", classNode.getName(), e);
            }
        }

        if (ProbeZSConfig.dumpDZS) {
            IndentStringBuilder builder = new IndentStringBuilder();
            TypeNameContext context = new TypeNameContext(null);
            for (ZenExpandClassNode expandClassNode : builtinTypeExpansions.values()) {
                expandClassNode.setMentionedTypes(context);
            }
            context.toZenScript(builder, context);
            builder.interLine();
            for (ZenExpandClassNode expandClassNode : builtinTypeExpansions.values()) {
                expandClassNode.toZenScript(builder, context);
                builder.interLine();
            }
            try {
                FileUtils.createFile(dzsPath.resolve("expands.dzs"), builder.toString());
            } catch (IOException e) {
                ProbeZS.logger.error("Failed to output expands.dzs", e);
            }
        }
    }

    public void putGlobalInternalClass(Class<?> clazz) {
        if (!javaMap.containsKey(clazz.getName())) {
            ZenClassNode classNode = classes.computeIfAbsent(clazz.getName(), it -> new ZenClassNode(it, this));
            javaMap.put(clazz.getName(), classNode);
            classNode.readMembers(clazz, true);
        }
    }

    private void  registerPrimitiveClass(Class<?> javaClass, ZenClassNode node) {
        classes.put(node.getName(), node);
        javaMap.put(javaClass.getName(), node);
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
        javaMap.put(void.class.getName(), voidNode);
        javaMap.put(Void.class.getName(), voidNode);
        registerPrimitiveClass(String.class, stringNode);
        registerPrimitiveClass(CharSequence.class, stringNode);
        registerPrimitiveClass(IntRange.class, new ZenIntRangeNode(this));
        javaMap.put(Object.class.getName(), anyClass);
    }

    private void readBuiltinTypeExpansion(String name, Class<?> clazz) {
        ZenExpandClassNode expandClassNode = builtinTypeExpansions.computeIfAbsent(name, it -> new ZenExpandClassNode(it, this));
        expandClassNode.readMembers(clazz, false);
    }
}
