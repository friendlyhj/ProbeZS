package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.*;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenClassNode implements IZenDumpable {
    private final String name;
    private final ZenClassTree tree;
    protected final List<LazyZenClassNode> extendClasses = new ArrayList<>();
    protected final List<ZenMemberNode> members = new ArrayList<>();
    protected final Map<String, ZenPropertyNode> properties = new LinkedHashMap<>();
    private Method lambdaMethod;
    private boolean hasAnnotatedLambdaMethod;

    public ZenClassNode(String name, ZenClassTree tree) {
        this.name = name;
        this.tree = tree;
    }

    public String getName() {
        return name;
    }

    public ZenClassTree getTree() {
        return tree;
    }

    public void readExtendClasses(Class<?> clazz) {
        extendClasses.add(tree.createLazyClassNode(clazz.getSuperclass()));
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
            if (clazz.isAnnotationPresent(IterableSimple.class)) {
                ZenMemberNode iteratorMember = new ZenMemberNode("iterator", "[" + clazz.getAnnotation(IterableSimple.class).value() + "]", Collections.emptyList(), false);
                iteratorMember.addAnnotation("operator", "ITERABLE");
                iteratorMember.addAnnotation("hidden");
                members.add(iteratorMember);
            }
            if (clazz.isAnnotationPresent(IterableList.class)) {
                ZenMemberNode iteratorMember = new ZenMemberNode("iterator", "[" + clazz.getAnnotation(IterableList.class).value() + "]", Collections.emptyList(), false);
                iteratorMember.addAnnotation("operator", "ITERABLE");
                iteratorMember.addAnnotation("hidden");
                members.add(iteratorMember);
            }
            if (clazz.isAnnotationPresent(IterableMap.class)) {
                IterableMap iterableMap = clazz.getAnnotation(IterableMap.class);
                ZenMemberNode iteratorMember = new ZenMemberNode("iterator", String.format("%s[%s]", iterableMap.value(), iterableMap.key()), Collections.emptyList(), false);
                iteratorMember.addAnnotation("operator", "ITERABLEMAP");
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
                LazyZenClassNode type = tree.createLazyClassNode(method.getGenericReturnType());
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
            ZenMemberNode lambdaNode = ZenMemberNode.readInternal(lambdaMethod, tree, lambdaMethod.getName(), false, false);
            lambdaNode.addAnnotation("lambda");
            lambdaNode.addAnnotation("hidden");
            members.add(lambdaNode);
        }
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        String extendInformation = extendClasses.stream()
                .filter(LazyZenClassNode::isExisted)
                .map(LazyZenClassNode::get)
                .map(ZenClassNode::getName)
                .filter(Predicate.isEqual("any").negate())
                .collect(Collectors.joining(" "));
        if (!extendInformation.isEmpty()) {
            sb.append("#extends ").append(extendInformation).nextLine();
        }
        sb.append("zenClass ");
        sb.append(name);
        sb.append(" {");
        sb.push();
        for (ZenPropertyNode propertyNode : properties.values()) {
            propertyNode.toZenScript(sb);
            sb.nextLine();
        }
        for (ZenMemberNode member : members) {
            sb.interLine();
            member.toZenScript(sb);
        }
        sb.pop();
        sb.append("}");
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
}
