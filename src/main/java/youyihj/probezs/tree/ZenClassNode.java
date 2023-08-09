package youyihj.probezs.tree;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import stanhebben.zenscript.annotations.*;
import youyihj.probezs.tree.primitive.IPrimitiveType;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenClassNode implements IZenDumpable, IHasImportMembers, Comparable<ZenClassNode> {
    private static final Pattern QUALIFIED_NAME_REGEX = Pattern.compile("(\\w+\\.)*(\\w+)");

    private final String name;
    private final ZenClassTree tree;
    private final String qualifiedName;

    protected final List<LazyZenClassNode> extendClasses = new ArrayList<>();
    protected final List<ZenMemberNode> members = new ArrayList<>();
    protected final List<ZenConstructorNode> constructors = new ArrayList<>();
    protected final Map<String, ZenPropertyNode> properties = new LinkedHashMap<>();
    private Method lambdaMethod;
    private boolean hasAnnotatedLambdaMethod;

    public ZenClassNode(String name, ZenClassTree tree) {
        this.name = name;
        this.tree = tree;
        this.qualifiedName = processQualifiedName(name);
    }

    public String getName() {
        return name;
    }

    public ZenClassTree getTree() {
        return tree;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void readExtendClasses(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            extendClasses.add(tree.createLazyClassNode(superclass));
        }
        for (Class<?> anInterface : clazz.getInterfaces()) {
            extendClasses.add(tree.createLazyClassNode(anInterface));
        }
        lambdaMethod = findLambdaMethod(clazz);
    }

    public void readMembers(Class<?> clazz, boolean isClass) {
        if (isClass) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isPublic(field.getModifiers())) continue;
                if (field.isAnnotationPresent(ZenProperty.class)) {
                    LazyZenClassNode type = tree.createLazyClassNode(field.getGenericType());
                    String name = field.getAnnotation(ZenProperty.class).value();
                    if (name.isEmpty()) {
                        name = field.getName();
                    }
                    ZenPropertyNode propertyNode = new ZenPropertyNode(type, name);
                    propertyNode.setStatic(Modifier.isStatic(field.getModifiers()));
                    propertyNode.setHasGetter(true);
                    propertyNode.setHasSetter(!Modifier.isFinal(field.getModifiers()));
                    properties.put(name, propertyNode);
                }
            }
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (!Modifier.isPublic(constructor.getModifiers())) continue;
                if (constructor.isAnnotationPresent(ZenConstructor.class)) {
                    constructors.add(ZenConstructorNode.read(constructor, tree));
                }
            }
            if (clazz.isAnnotationPresent(IterableSimple.class)) {
                String value = clazz.getAnnotation(IterableSimple.class).value();
                ZenMemberNode iteratorMember =
                        new ZenMemberNode("iterator", () -> LazyZenClassNode.Result.compound("[" + processQualifiedName(value) + "]", tree.getClasses().get(value)), Collections.emptyList(), false);
                iteratorMember.addAnnotation("foreach");
                iteratorMember.addAnnotation("hidden");
                members.add(iteratorMember);
            }
            if (clazz.isAnnotationPresent(IterableList.class)) {
                String value = clazz.getAnnotation(IterableList.class).value();
                ZenMemberNode iteratorMember =
                        new ZenMemberNode("iterator", () -> LazyZenClassNode.Result.compound("[" + value + "]", tree.getClasses().get(value)), Collections.emptyList(), false);
                iteratorMember.addAnnotation("foreach");
                iteratorMember.addAnnotation("hidden");
                members.add(iteratorMember);
            }
            if (clazz.isAnnotationPresent(IterableMap.class)) {
                IterableMap iterableMap = clazz.getAnnotation(IterableMap.class);
                String key = iterableMap.key();
                String value = iterableMap.value();
                ZenMemberNode iteratorMember =
                        new ZenMemberNode("iterator",
                                () -> LazyZenClassNode.Result.compound(String.format("%s[%s]", processQualifiedName(value), processQualifiedName(key)), tree.getClasses().get(key), tree.getClasses().get(value)),
                                Collections.emptyList(), false);
                iteratorMember.addAnnotation("foreachMap");
                iteratorMember.addAnnotation("hidden");
                members.add(iteratorMember);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            if (method.isAnnotationPresent(ZenGetter.class)) {
                LazyZenClassNode type = tree.createLazyClassNode(method.getGenericReturnType());
                String name = method.getAnnotation(ZenGetter.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                ZenPropertyNode propertyNode = properties.computeIfAbsent(name, it -> new ZenPropertyNode(type, it));
                propertyNode.setHasGetter(true);
            }
            if (method.isAnnotationPresent(ZenSetter.class)) {
                LazyZenClassNode type = tree.createLazyClassNode(method.getGenericParameterTypes()[isClass ? 0 : 1]);
                String name = method.getAnnotation(ZenSetter.class).value();
                if (name.isEmpty()) {
                    name = method.getName();
                }
                ZenPropertyNode propertyNode = properties.computeIfAbsent(name, it -> new ZenPropertyNode(type, it));
                propertyNode.setHasSetter(true);
            }
            ZenMemberNode memberNode = ZenMemberNode.read(method, tree, isClass);
            if (memberNode != null) {
                members.add(memberNode);
                if (isClass && Objects.equals(method, lambdaMethod)) {
                    memberNode.addAnnotation("lambda");
                    hasAnnotatedLambdaMethod = true;
                }
            }
        }
        if (isClass && lambdaMethod != null && !hasAnnotatedLambdaMethod) {
            ZenMemberNode lambdaNode = ZenMemberNode.readDirectly(lambdaMethod, tree, lambdaMethod.getName(), false, false);
            lambdaNode.addAnnotation("lambda");
            lambdaNode.addAnnotation("hidden");
            members.add(lambdaNode);
        }
    }

    public Set<ZenClassNode> getImportMembers() {
        Set<ZenClassNode> imports = new TreeSet<ZenClassNode>() {
            @Override
            public boolean add(ZenClassNode node) {
                if (node instanceof IPrimitiveType || node == ZenClassNode.this) {
                    return false;
                } else {
                    return super.add(node);
                }
            }
        };
        this.fillImportMembers(imports);
        for (ZenPropertyNode property : properties.values()) {
            property.fillImportMembers(imports);
        }
        for (ZenMemberNode member : members) {
            member.fillImportMembers(imports);
        }
        for (ZenConstructorNode constructor : constructors) {
            constructor.fillImportMembers(imports);
        }
        return imports;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        Set<ZenClassNode> imports = getImportMembers();
        for (ZenClassNode anImport : imports) {
            sb.append("import ").append(anImport.getName()).append(";").nextLine();
        }
        sb.nextLine();
        String extendInformation = extendClasses.stream()
                .filter(LazyZenClassNode::isExisted)
                .map(LazyZenClassNode::get)
                .map(it -> it.getTypeVariables().get(0).getName())
                .collect(Collectors.joining(" "));
        if (!extendInformation.isEmpty()) {
            sb.append("#extends ").append(extendInformation).nextLine();
        }
//        if (lambdaMethod != null) {
//            sb.append("#functionalInterface").nextLine();
//        }
        sb.append("zenClass ");
        sb.append(getQualifiedName());
        sb.append(" {");
        sb.push();
        for (ZenPropertyNode propertyNode : properties.values()) {
            propertyNode.toZenScript(sb);
            sb.nextLine();
        }
        for (ZenConstructorNode constructor : constructors) {
            sb.interLine();
            constructor.toZenScript(sb);
        }

        for (ZenMemberNode member : members) {
            sb.interLine();
            member.toZenScript(sb);
        }
        sb.pop();
        sb.append("}");
    }

    @Override
    public void fillImportMembers(Set<ZenClassNode> members) {
        for (LazyZenClassNode extendClass : extendClasses) {
            members.addAll(extendClass.get().getTypeVariables());
        }
    }

    private Method findLambdaMethod(Class<?> clazz) {
        Method lambdaMethod = null;
        if (clazz.isInterface()) {
            for (Method method : clazz.getMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isAbstract(modifiers)) {
                    if (lambdaMethod != null) {
                        return null;
                    }
                    lambdaMethod = method;
                }
            }
        }
        return lambdaMethod;
    }

    private static String processQualifiedName(String name) {
        Matcher matcher = QUALIFIED_NAME_REGEX.matcher(name);
        if (matcher.find()) {
            return matcher.group(2);
        } else {
            return name;
        }
    }

    @Override
    public int compareTo(ZenClassNode o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZenClassNode classNode = (ZenClassNode) o;
        return Objects.equals(name, classNode.name) && Objects.equals(tree, classNode.tree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tree);
    }

    public static class Serializer implements JsonSerializer<ZenClassNode> {

        @Override
        public JsonElement serialize(ZenClassNode src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("name", src.getQualifiedName());
            JsonArray imports = new JsonArray();
            for (ZenClassNode importMember : src.getImportMembers()) {
                imports.add(importMember.getName());
            }
            json.add("imports", imports);
            json.add("extends", context.serialize(src.extendClasses, new TypeToken<List<LazyZenClassNode>>() {
            }.getType()));
            json.add("properties", context.serialize(src.properties.values(), new TypeToken<Collection<ZenPropertyNode>>() {
            }.getType()));
            json.add("constructors", context.serialize(src.constructors, new TypeToken<List<ZenConstructorNode>>() {
            }.getType()));
            json.add("members", context.serialize(src.members, new TypeToken<List<ZenMemberNode>>() {
            }.getType()));
            return json;
        }
    }

}
