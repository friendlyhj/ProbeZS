package youyihj.probezs.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import crafttweaker.util.IEventHandler;
import youyihj.probezs.util.CastRuleType;
import youyihj.probezs.util.IntersectionType;
import youyihj.zenutils.impl.member.ClassData;
import youyihj.zenutils.impl.member.LiteralType;
import youyihj.zenutils.impl.member.bytecode.MethodParameterParser;
import youyihj.zenutils.impl.util.InternalUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class JavaTypeMirror implements Supplier<JavaTypeMirror.Result> {
    private final Type type;
    private final ZenClassTree classTree;
    private Result result;

    private boolean init = false;
    private boolean existed = false;

    public JavaTypeMirror(Type type, ZenClassTree classTree) {
        this.type = type;
        this.classTree = classTree;
    }

    @Override
    public Result get() {
        if (init) {
            return result;
        }
        throw new IllegalStateException();
    }

    public boolean isExisted() {
        if (init) {
            return existed;
        }
        throw new IllegalStateException();
    }

    void fresh() {
        init = true;
        result = getResult(type);
        existed = !(result instanceof MissingResult);
    }

    private Result getResult(Type type) {
        Map<String, ZenClassNode> javaMap = classTree.getJavaMap();
        try {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isArray()) {
                    Result baseClass = getResult(clazz.getComponentType());
                    return Result.compound("%s[]", baseClass);
                } else {
                    ZenClassNode zsClass = javaMap.get(((Class<?>) type).getName());
                    // the class is exposed to zs
                    if (zsClass != null) {
                        return Result.single(zsClass);
                    }
                    // the class isn't exposed to zs, but its super class and implement interfaces may be exposed
                    List<Class<?>> exposedParents = collectExposedParents(clazz, new ArrayList<>());
                    if (exposedParents.isEmpty()) {
                        return Result.missing(classTree, clazz);
                    }
                    return Result.intersection(
                            exposedParents.stream()
                                    .map(this::getResult)
                                    .collect(Collectors.toList())
                    );
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] arguments = parameterizedType.getActualTypeArguments();
                if (parameterizedType.getRawType() == List.class) {
                    Result baseClass = getResult(arguments[0]);
                    return Result.compound("[%s]", baseClass);
                }
                if (parameterizedType.getRawType() == IEventHandler.class) {
                    Result baseClass = getResult(arguments[0]);
                    return Result.compound("function(%s)void", baseClass);
                }
                if (parameterizedType.getRawType() == Map.class) {
                    Result keyClass = getResult(arguments[0]);
                    Result valueClass = getResult(arguments[1]);
                    return Result.compound("%s[%s]", valueClass, keyClass);
                }
                return getResult(parameterizedType.getRawType());
            } else if (type instanceof IntersectionType) {
                Type[] compoundTypes = ((IntersectionType) type).getCompoundTypes();
                return Result.intersection(
                        Arrays.stream(compoundTypes)
                                .map(this::getResult)
                                .collect(Collectors.toList())
                );
            } else if (type instanceof CastRuleType) {
                return Result.castResult(
                        ((CastRuleType) type).getTypes()
                                .stream()
                                .map(this::getResult)
                                .collect(Collectors.toList())
                );
            } else if (type instanceof LiteralType) {
                String name = type.getTypeName();
                switch (name) {
                    case "I":
                        return getResult(int.class);
                    case "Z":
                        return getResult(boolean.class);
                    case "D":
                        return getResult(double.class);
                    case "F":
                        return getResult(float.class);
                    case "J":
                        return getResult(long.class);
                    case "B":
                        return getResult(byte.class);
                    case "S":
                        return getResult(short.class);
                    case "C":
                        return getResult(char.class);
                }
                if (name.startsWith("[")) {
                    // array type
                    String elementTypeName = name.substring(1);
                    LiteralType elementType = new LiteralType(elementTypeName);
                    Result baseClass = getResult(elementType);
                    return Result.compound("%s[]", baseClass);
                }
                if (name.startsWith("L") && name.endsWith(";")) {
                    if (!name.contains("<")) {
                        String className = name.substring(1, name.length() - 1).replace('/', '.');
                        ZenClassNode zsClass = javaMap.get(className);
                        if (zsClass != null) {
                            return Result.single(zsClass);
                        }
                    } else {
                        String genericInfo = name.substring(name.indexOf("<") + 1, name.lastIndexOf(">"));
                        String rawClassName = name.substring(1, name.indexOf("<")).replace('/', '.');
                        List<String> genericTypes = new MethodParameterParser("(" + genericInfo + ")").parse();
                        ClassData rawClassData = InternalUtils.getClassDataFetcher().forName(rawClassName);
                        if (genericTypes.size() == 1) {
                            if (InternalUtils.getClassDataFetcher().forClass(List.class).isAssignableFrom(rawClassData)) {
                                Result baseClass = getResult(new LiteralType(genericTypes.get(0)));
                                return Result.compound("[%s]", baseClass);
                            }
                            if (InternalUtils.getClassDataFetcher().forClass(IEventHandler.class).isAssignableFrom(rawClassData)) {
                                Result baseClass = getResult(new LiteralType(genericTypes.get(0)));
                                return Result.compound("function(%s)void", baseClass);
                            }
                        }
                        if (genericTypes.size() == 2) {
                            if (InternalUtils.getClassDataFetcher().forClass(Map.class).isAssignableFrom(rawClassData)) {
                                Result keyClass = getResult(new LiteralType(genericTypes.get(0)));
                                Result valueClass = getResult(new LiteralType(genericTypes.get(1)));
                                return Result.compound("%s[%s]", valueClass, keyClass);
                            }
                        }
                        ZenClassNode zsClass = javaMap.get(rawClassName);
                        if (zsClass != null) {
                            return Result.single(zsClass);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return Result.missing(classTree, type);
    }

    public List<Class<?>> collectExposedParents(Class<?> clazz, List<Class<?>> list) {
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (classTree.getJavaMap().containsKey(anInterface)) {
                addInteractionClasses(list, anInterface);
            } else {
                collectExposedParents(anInterface, list);
            }
        }
        if (!clazz.isInterface()) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                if (classTree.getJavaMap().containsKey(superclass)) {
                    addInteractionClasses(list, superclass);
                } else {
                    collectExposedParents(superclass, list);
                }
            }
        }
        return list;
    }

    private void addInteractionClasses(List<Class<?>> classes, Class<?> clazz) {
        boolean overlap = classes.stream().anyMatch(clazz::isAssignableFrom);
        if (!overlap) {
            classes.add(clazz);
        }
    }

    public interface Result {
        String getFullName();

        String getQualifiedName(TypeNameContext context);

        List<ZenClassNode> getTypeVariables();

        static Result single(ZenClassNode classNode) {
            return new SingleResult(classNode);
        }

        static Result missing(ZenClassTree tree, Type originType) {
//            ProbeZS.logger.warn("Do not know zenscript type for {}", originType.getTypeName());
            return new MissingResult(tree);
        }

        static Result compound(String format, Result... results) {
            return new CompoundResult(format, results);
        }

        static Result intersection(List<Result> results) {
            return new MultipleResult(results, " & ");
        }

        static Result castResult(List<Result> results) {
            return new MultipleResult(results, ", ");
        }
    }

    private static class SingleResult implements Result {
        private final ZenClassNode classNode;

        public SingleResult(ZenClassNode classNode) {
            this.classNode = classNode;
        }

        @Override
        public String getFullName() {
            return classNode.getName();
        }

        @Override
        public String getQualifiedName(TypeNameContext context) {
            return context.getTypeName(classNode);
        }

        @Override
        public List<ZenClassNode> getTypeVariables() {
            return Collections.singletonList(classNode);
        }
    }


    private static class MissingResult implements Result {
        private final ZenClassTree tree;

        private MissingResult(ZenClassTree tree) {
            this.tree = tree;
        }

        @Override
        public String getFullName() {
            return "any";
        }

        @Override
        public String getQualifiedName(TypeNameContext context) {
            return "any";
        }

        @Override
        public List<ZenClassNode> getTypeVariables() {
            return Collections.singletonList(tree.getAnyClass());
        }
    }

    private static class CompoundResult implements Result {
        private final String format;
        private final List<Result> results;

        public CompoundResult(String format, Result... results) {
            this.format = format;
            this.results = Arrays.asList(results);
        }

        @Override
        public String getFullName() {
            return String.format(format, results.stream().map(Result::getFullName).toArray());
        }

        @Override
        public String getQualifiedName(TypeNameContext context) {
            return String.format(format, results.stream().map(context::getTypeName).toArray());
        }

        @Override
        public List<ZenClassNode> getTypeVariables() {
            return results.stream()
                    .flatMap(it -> it.getTypeVariables().stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private static class MultipleResult implements Result {
        private final List<Result> results;
        private final String delimiter;

        public MultipleResult(List<Result> results, String delimiter) {
            this.results = results;
            this.delimiter = delimiter;
        }

        @Override
        public String getFullName() {
            return results.stream()
                    .map(Result::getFullName)
                    .collect(Collectors.joining(delimiter));
        }

        @Override
        public String getQualifiedName(TypeNameContext context) {
            return results.stream()
                    .map(context::getTypeName)
                    .collect(Collectors.joining(delimiter));
        }

        @Override
        public List<ZenClassNode> getTypeVariables() {
            return results.stream()
                    .flatMap(it -> it.getTypeVariables().stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    public static class Serializer implements JsonSerializer<JavaTypeMirror> {

        @Override
        public JsonElement serialize(JavaTypeMirror src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.get().getFullName());
        }
    }
}
