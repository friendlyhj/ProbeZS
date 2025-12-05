package youyihj.probezs.tree;

import com.google.common.base.Suppliers;
import com.google.common.collect.Multimap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;
import youyihj.zenutils.impl.member.ClassData;
import youyihj.zenutils.impl.member.ExecutableData;
import youyihj.zenutils.impl.member.FieldData;
import youyihj.zenutils.impl.member.LookupRequester;
import youyihj.zenutils.impl.zenscript.nat.MCPReobfuscation;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class ZenNativeClassNode extends ZenClassNode {
    @SuppressWarnings("unchecked")
    private static final Supplier<Map<String, String>> METHOD_REMAP = Suppliers.memoize(() ->
            {
                Map<String, String> map = new HashMap<>();
                Multimap<String, String> origin = ((CompletableFuture<Pair<Multimap<String, String>, Multimap<String, String>>>) ObfuscationReflectionHelper.getPrivateValue(MCPReobfuscation.class, MCPReobfuscation.INSTANCE, "mappers"))
                        .join()
                        .getLeft();
                origin.forEach((k, v) -> map.put(v, k));
                return map;
            }
    );

    public ZenNativeClassNode(String name, ZenClassTree tree) {
        super("native." + name, tree);
    }

    public void fill(ClassData classData) {
        // superClass
        ClassData superClass = classData.superClass();
        if (superClass != null && !superClass.name().equals("java.lang.Object")) {
            extendClasses.add(tree.createJavaTypeMirror(superClass.javaType()));
        }
        for (ClassData anInterface : classData.interfaces()) {
            extendClasses.add(tree.createJavaTypeMirror(anInterface.javaType()));
        }

        // constructors
        for (ExecutableData constructor : classData.constructors(LookupRequester.PUBLIC)) {
            constructors.add(ZenConstructorNode.read(constructor, tree));
        }

        // properties
        for (FieldData field : classData.fields(LookupRequester.PUBLIC)) {
            if (field.declaringClass() != classData) continue;
            ZenPropertyNode propertyNode = new ZenPropertyNode(tree.createJavaTypeMirror(field.type()
                    .javaType()), field.name());
            propertyNode.setHasGetter(true);
            propertyNode.setHasSetter(!Modifier.isFinal(field.modifiers()));
            propertyNode.setStatic(Modifier.isStatic(field.modifiers()));
            properties.put(field.name(), propertyNode);
        }

        // methods
        for (ExecutableData method : classData.methods(LookupRequester.PUBLIC)) {
            if (method.declaringClass() != classData) continue;
            String name = method.name();
            if (METHOD_REMAP.get().containsKey(name)) {
                name = METHOD_REMAP.get().get(name);
            }
            ZenMemberNode methodNode = ZenMemberNode.readDirectly(method, tree, name, Modifier.isStatic(method.modifiers()), false);
            members.add(methodNode);

            if (name.startsWith("get") && method.parameterCount() == 0 && name.length() > 3) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                ZenPropertyNode propertyNode = properties.computeIfAbsent(propName, it -> new ZenPropertyNode(tree.createJavaTypeMirror(method.returnType()
                        .javaType()), propName));
                propertyNode.setHasGetter(true);
                propertyNode.setStatic(Modifier.isStatic(method.modifiers()));
            } else if (name.startsWith("is") && method.parameterCount() == 0 && name.length() > 2) {
                String propName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                ZenPropertyNode propertyNode = properties.computeIfAbsent(propName, it -> new ZenPropertyNode(tree.createJavaTypeMirror(method.returnType()
                        .javaType()), propName));
                propertyNode.setHasGetter(true);
                propertyNode.setStatic(Modifier.isStatic(method.modifiers()));
            } else if (name.startsWith("set") && method.parameterCount() == 1 && name.length() > 3) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                ZenPropertyNode propertyNode = properties.computeIfAbsent(propName, it -> new ZenPropertyNode(tree.createJavaTypeMirror(method.parameters()
                        .get(0)
                        .javaType()), propName));
                propertyNode.setHasSetter(true);
                propertyNode.setStatic(Modifier.isStatic(method.modifiers()));
            }
        }
    }
}
