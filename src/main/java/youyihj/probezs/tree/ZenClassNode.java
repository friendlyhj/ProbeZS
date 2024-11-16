package youyihj.probezs.tree;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import stanhebben.zenscript.annotations.*;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.ProbeZSConfig;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.member.FieldData;
import youyihj.probezs.util.IndentStringBuilder;
import youyihj.probezs.util.ZenOperators;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenClassNode implements IZenDumpable, ITypeNameContextAcceptor, Comparable<ZenClassNode> {
    private static final Pattern QUALIFIED_NAME_REGEX = Pattern.compile("((\\w+\\.)*\\w+)\\.(\\w+)");

    private final String name;
    private final ZenClassTree tree;
    private final String qualifiedName;
    private final String packageName;

    protected final List<JavaTypeMirror> extendClasses = new ArrayList<>();
    protected final List<ZenMemberNode> members = new ArrayList<>();
    protected final List<ZenConstructorNode> constructors = new ArrayList<>();
    protected final Map<String, ZenPropertyNode> properties = new LinkedHashMap<>();
    protected final Multimap<String, ZenOperatorNode> operators = HashMultimap.create();
    private ZenOperatorNode.As caster;
    private String owner;

    public ZenClassNode(String name, ZenClassTree tree) {
        this.name = name;
        this.tree = tree;
        Matcher matcher = QUALIFIED_NAME_REGEX.matcher(name);
        if (matcher.find()) {
            this.qualifiedName = matcher.group(3);
            this.packageName = matcher.group(1);
        } else {
            this.qualifiedName = name;
            this.packageName = null;
        }
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
            extendClasses.add(tree.createJavaTypeMirror(superclass));
        }
        for (Class<?> anInterface : clazz.getInterfaces()) {
            extendClasses.add(tree.createJavaTypeMirror(anInterface));
        }
    }

    public void readMembers(Class<?> clazz, boolean isClass) {
        if (isClass) {
            readConstructors(clazz);
            readProperties(clazz);
            readIteratorOperators(clazz);
            if (ProbeZSConfig.outputSourceExpansionMembers) {
                owner = ProbeZS.instance.getClassOwner(clazz);
            }
        }
        boolean findingLambdaFrom = isClass;
        ExecutableData lambdaForm = null;
        ZenMemberNode lambdaFormZenCode = null;
        for (ExecutableData method : ProbeZS.getMemberFactory().getMethods(clazz)) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            readGetter(method, isClass);
            readSetter(method, isClass);
            ZenMemberNode currentMember = readMethod(method, isClass);
            if (findingLambdaFrom && Modifier.isAbstract(method.getModifiers())) {
                if (lambdaForm == null) {
                    lambdaForm = method;
                    lambdaFormZenCode = currentMember;
                } else {
                    lambdaForm = null;
                    lambdaFormZenCode = null;
                    findingLambdaFrom = false;
                }
            }
            readOperator(method, isClass);
            readCaster(method);
        }
        if (lambdaForm != null) {
            if (lambdaFormZenCode != null) {
                lambdaFormZenCode.setLambda();
            } else {
                lambdaFormZenCode = ZenMemberNode.readDirectly(lambdaForm, tree, "", false, false);
                lambdaFormZenCode.setLambda();
                members.add(lambdaFormZenCode);
            }
        }
    }

    public TypeNameContext getTypeNameContext() {
        TypeNameContext context = new TypeNameContext(this);
        setMentionedTypes(context);
        return context;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb, TypeNameContext context) {
        if (packageName != null) {
            sb.append("package ").append(packageName).append(";");
            sb.interLine();
        }
        context.toZenScript(sb, context);
        sb.interLine();
        String extendInformation = extendClasses.stream()
                .filter(JavaTypeMirror::isExisted)
                .map(JavaTypeMirror::get)
                .map(context::getTypeName)
                .collect(Collectors.joining(", "));
        sb.append("zenClass ");
        sb.append(getQualifiedName());
        if (!extendInformation.isEmpty()) {
            sb.append(" extends ");
            sb.append(extendInformation);
        }
        sb.append(" {");
        sb.push();
        for (ZenPropertyNode propertyNode : properties.values()) {
            sb.nextLine();
            removeSelfExpansion(propertyNode);
            propertyNode.toZenScript(sb, context);
        }
        if (!properties.isEmpty()) {
            sb.interLine();
        }
        for (ZenConstructorNode constructor : constructors) {
            constructor.toZenScript(sb, context);
            sb.interLine();
        }
        for (ZenMemberNode member : members) {
            removeSelfExpansion(member);
            member.toZenScript(sb, context);
            sb.interLine();
        }
        for (ZenOperatorNode operator : operators.values()) {
            removeSelfExpansion(operator);
            operator.toZenScript(sb, context);
            sb.interLine();
        }
        sb.pop();
        sb.append("}");
    }

    @Override
    public void setMentionedTypes(TypeNameContext context) {
        for (JavaTypeMirror extendClass : extendClasses) {
            context.addClasses(extendClass.get().getTypeVariables());
        }
        for (ZenPropertyNode property : properties.values()) {
            property.setMentionedTypes(context);
        }
        for (ZenMemberNode member : this.members) {
            member.setMentionedTypes(context);
        }
        for (ZenConstructorNode constructor : constructors) {
            constructor.setMentionedTypes(context);
        }
        for (ZenOperatorNode operator : operators.values()) {
            operator.setMentionedTypes(context);
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

    private void readProperties(Class<?> clazz) {
        for (FieldData field : ProbeZS.getMemberFactory().getFields(clazz)) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (field.isAnnotationPresent(ZenProperty.class)) {
                JavaTypeMirror type = tree.createJavaTypeMirror(field.getType());
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
    }

    private void readConstructors(Class<?> clazz) {
        for (ExecutableData constructor : ProbeZS.getMemberFactory().getConstructors(clazz)) {
            if (!Modifier.isPublic(constructor.getModifiers())) continue;
            if (constructor.isAnnotationPresent(ZenConstructor.class)) {
                constructors.add(ZenConstructorNode.read(constructor, tree));
            }
        }
    }

    private void readIteratorOperators(Class<?> clazz) {
        if (clazz.isAnnotationPresent(IterableSimple.class)) {
            String value = clazz.getAnnotation(IterableSimple.class).value();
            operators.put("for",
                    new ZenOperatorNode(
                            "for", Collections.singletonList(new ZenParameterNode(() -> "element", () -> JavaTypeMirror.Result.single(tree.getClasses()
                            .get(value)), null, false)),
                            tree.createJavaTypeMirror(void.class)
                    )
            );
        }
        if (clazz.isAnnotationPresent(IterableList.class)) {
            String value = clazz.getAnnotation(IterableList.class).value();
            operators.put("for",
                    new ZenOperatorNode(
                            "for", Collections.singletonList(new ZenParameterNode(() -> "element", () -> JavaTypeMirror.Result.single(tree.getClasses()
                            .get(value)), null, false)),
                            tree.createJavaTypeMirror(void.class)
                    )
            );
        }
        if (clazz.isAnnotationPresent(IterableMap.class)) {
            IterableMap iterableMap = clazz.getAnnotation(IterableMap.class);
            String key = iterableMap.key();
            String value = iterableMap.value();
            operators.put("for",
                    new ZenOperatorNode(
                            "for", Arrays.asList(
                            new ZenParameterNode(() -> "key", () -> JavaTypeMirror.Result.single(tree.getClasses().get(key)), null, false),
                            new ZenParameterNode(() -> "value", () -> JavaTypeMirror.Result.single(tree.getClasses().get(value)), null, false)
                    ), tree.createJavaTypeMirror(void.class)
                    )
            );
        }
    }

    private void readGetter(ExecutableData method, boolean isClass) {
        if (method.isAnnotationPresent(ZenGetter.class)) {
            JavaTypeMirror type = tree.createJavaTypeMirror(method.getReturnType());
            String name = method.getAnnotation(ZenGetter.class).value();
            if (name.isEmpty()) {
                name = method.getName();
            }
            ZenPropertyNode propertyNode = properties.computeIfAbsent(name, it -> new ZenPropertyNode(type, it));
            propertyNode.setHasGetter(true);
            if (!isClass) {
                readExpansionExecutableOwner(method, propertyNode);
            }
        }
    }

    private void readSetter(ExecutableData method, boolean isClass) {
        if (method.isAnnotationPresent(ZenSetter.class)) {
            JavaTypeMirror type = tree.createJavaTypeMirror(method.getParameters()[isClass ? 0 : 1].getGenericType());
            String name = method.getAnnotation(ZenSetter.class).value();
            if (name.isEmpty()) {
                name = method.getName();
            }
            ZenPropertyNode propertyNode = properties.computeIfAbsent(name, it -> new ZenPropertyNode(type, it));
            propertyNode.setHasSetter(true);
            if (!isClass) {
                readExpansionExecutableOwner(method, propertyNode);
            }
        }
    }

    private void readCaster(ExecutableData method) {
        if (method.isAnnotationPresent(ZenCaster.class)) {
            Type returnType = method.getReturnType();
            if (caster == null) {
                caster = new ZenOperatorNode.As(tree);
                operators.put("as", caster);
            }
            caster.appendCastType(returnType);
        }
    }

    private void readOperator(ExecutableData method, boolean isClass) {
        int startIndex = isClass ? 0 : 1;
        Map<String, Type> operatorNamesAndTypes = new HashMap<>();
        if (method.isAnnotationPresent(ZenOperator.class)) {
            OperatorType operatorType = method.getAnnotation(ZenOperator.class).value();
            switch (operatorType) {
                case EQUALS:
                    operatorNamesAndTypes.put("==", boolean.class);
                    operatorNamesAndTypes.put("!=", boolean.class);
                    break;
                case COMPARE:
                    operatorNamesAndTypes.put("==", boolean.class);
                    operatorNamesAndTypes.put("!=", boolean.class);
                    operatorNamesAndTypes.put(">", boolean.class);
                    operatorNamesAndTypes.put("<", boolean.class);
                    operatorNamesAndTypes.put(">=", boolean.class);
                    operatorNamesAndTypes.put("<=", boolean.class);
                    break;
                default:
                    operatorNamesAndTypes.put(ZenOperators.getZenScriptFormat(operatorType), method.getReturnType());
                    break;
            }
        }
        if (method.isAnnotationPresent(ZenMemberGetter.class)) {
            operatorNamesAndTypes.put(".", method.getReturnType());
        }
        if (method.isAnnotationPresent(ZenMemberSetter.class)) {
            operatorNamesAndTypes.put(".=", method.getReturnType());
        }
        operatorNamesAndTypes.forEach((name, type) -> {
            ZenOperatorNode operator = new ZenOperatorNode(
                    name,
                    ZenParameterNode.read(method, startIndex, tree),
                    tree.createJavaTypeMirror(type)
            );
            operators.put(name, operator);
            if (!isClass) {
                readExpansionExecutableOwner(method, operator);
            }
        });
    }

    private ZenMemberNode readMethod(ExecutableData method, boolean isClass) {
        ZenMemberNode memberNode = ZenMemberNode.read(method, tree, isClass);
        if (memberNode != null) {
            if (!isClass) {
                readExpansionExecutableOwner(method, memberNode);
            }
            members.add(memberNode);
        }
        return memberNode;
    }

    private void readExpansionExecutableOwner(ExecutableData method, IMaybeExpansionMember expansionMember) {
        if (ProbeZSConfig.outputSourceExpansionMembers) {
            String classOwner = ProbeZS.instance.getClassOwner(method.getDecalredClass());
            expansionMember.setOwner(classOwner);
        }
    }

    private void removeSelfExpansion(IMaybeExpansionMember expansionMember) {
        if (Objects.equals(owner, expansionMember.getOwner())) {
            expansionMember.setOwner(null);
        }
    }
}
