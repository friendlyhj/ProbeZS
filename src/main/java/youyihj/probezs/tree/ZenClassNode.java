package youyihj.probezs.tree;

import stanhebben.zenscript.annotations.*;
import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenClassNode {
    private final String name;
    private final ZenClassTree tree;
    private final List<LazyZenClassNode> extendClasses = new ArrayList<>();

    private final List<ZenMemberNode> members = new ArrayList<>();
    private final Map<String, ZenPropertyNode> properties = new HashMap<>();

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
    }

    public void readMembers(Class<?> clazz, boolean isClass) {
        if (isClass) {
            for (Field field : clazz.getFields()) {
                if (field.isAnnotationPresent(ZenProperty.class)) {
                    LazyZenClassNode type = tree.createLazyClassNode(field.getGenericType());
                    String name = field.getAnnotation(ZenProperty.class).value();
                    if (name.isEmpty()) {
                        name = field.getName();
                    }
                    ZenPropertyNode propertyNode = new ZenPropertyNode(type, name);
                    propertyNode.setHasGetter(true);
                    propertyNode.setHasSetter(true);
                    properties.put(name, propertyNode);
                }
            }
        }
        for (Method method : clazz.getMethods()) {
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
            }
        }
    }

    public void toZenScript(IndentStringBuilder sb) {
        String extendInformation = extendClasses.stream()
                .filter(LazyZenClassNode::isExisted)
                .map(LazyZenClassNode::get)
                .map(ZenClassNode::getName)
                .collect(Collectors.joining(", "));
        sb.append("zenClass ");
        sb.append(name);
        sb.append(" {");
        sb.push();
        if (!extendInformation.isEmpty()) {
            sb.append("// extends: ").append(extendInformation).interLine();
        }
        for (ZenPropertyNode propertyNode : properties.values()) {
            propertyNode.toZenScript(sb);
            sb.nextLine();
        }
        for (ZenMemberNode member : members) {
            member.toZenScript(sb);
            sb.interLine();
        }
        sb.pop();
        sb.append("}");
    }
}
