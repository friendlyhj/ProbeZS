package youyihj.probezs.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import crafttweaker.util.IEventHandler;
import org.apache.commons.lang3.ArrayUtils;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.util.IntersectionType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author youyihj
 */
public class LazyZenClassNode implements Supplier<LazyZenClassNode.Result> {
    private final Type type;
    private final ZenClassTree classTree;
    private Result result;

    private boolean init = false;
    private boolean existed = false;

    public LazyZenClassNode(Type type, ZenClassTree classTree) {
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
        Map<Class<?>, ZenClassNode> javaMap = classTree.getJavaMap();
        try {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isArray()) {
                    Result baseClass = getResult(clazz.getComponentType());
                    return Result.compound(baseClass.getQualifiedName() + "[]", baseClass.getTypeVariableArray());
                } else {
                    ZenClassNode zsClass = javaMap.get(type);
                    // the class is exposed to zs
                    if (zsClass != null) {
                        return Result.single(zsClass);
                    }
                    // the class isn't exposed to zs, but its super class and implement interfaces may be exposed
                    Set<Class<?>> exposedParents = collectExposedParents(clazz, new HashSet<>());
                    if (exposedParents.isEmpty()) {
                        return Result.missing(classTree, clazz);
                    }
                    StringJoiner nameJoiner = new StringJoiner(" & ");
                    ZenClassNode[] typeVariables = exposedParents.stream()
                            .map(this::getResult)
                            .peek(it -> nameJoiner.add(it.getQualifiedName()))
                            .flatMap(it -> Arrays.stream(it.getTypeVariableArray()))
                            .toArray(ZenClassNode[]::new);
                    return Result.compound(nameJoiner.toString(), typeVariables);
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] arguments = parameterizedType.getActualTypeArguments();
                if (parameterizedType.getRawType() == List.class) {
                    Result baseClass = getResult(arguments[0]);
                    return Result.compound("[" + baseClass.getQualifiedName() + "]", baseClass.getTypeVariableArray());
                }
                if (parameterizedType.getRawType() == IEventHandler.class) {
                    Result baseClass = getResult(arguments[0]);
                    return Result.compound("function(" + baseClass.getQualifiedName() + ")void", baseClass.getTypeVariableArray());
                }
                if (parameterizedType.getRawType() == Map.class) {
                    Result keyClass = getResult(arguments[0]);
                    Result valueClass = getResult(arguments[1]);
                    return Result.compound(valueClass.getQualifiedName() + "[" + keyClass.getQualifiedName() + "]", ArrayUtils.addAll(keyClass.getTypeVariableArray(), valueClass.getTypeVariableArray()));
                }
                return getResult(parameterizedType.getRawType());
            } else if (type instanceof IntersectionType) {
                Type[] compoundTypes = ((IntersectionType) type).getCompoundTypes();
                StringJoiner nameJoiner = new StringJoiner(" & ");
                ZenClassNode[] typeVariables = Arrays.stream(compoundTypes)
                        .map(this::getResult)
                        .peek(it -> nameJoiner.add(it.getQualifiedName()))
                        .flatMap(it -> Arrays.stream(it.getTypeVariableArray()))
                        .distinct()
                        .toArray(ZenClassNode[]::new);
                return Result.compound(nameJoiner.toString(), typeVariables);
            }
        } catch (Exception ignored) {
        }
        return Result.missing(classTree, type);
    }

    private Set<Class<?>> collectExposedParents(Class<?> clazz, Set<Class<?>> set) {
        for (Class<?> anInterface : clazz.getInterfaces()) {
            if (classTree.getJavaMap().containsKey(anInterface)) {
                set.add(anInterface);
            } else {
                collectExposedParents(anInterface, set);
            }
        }
        if (!clazz.isInterface()) {
            for (Class<?> superClass = clazz.getSuperclass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (classTree.getJavaMap().containsKey(superClass)) {
                    set.add(superClass);
                    break;
                }
            }
        }
        return set;
    }

    public static class Result {
        private final String qualifiedName;
        private final ZenClassNode[] typeVariables;

        private Result(String qualifiedName, ZenClassNode... typeVariables) {
            this.qualifiedName = qualifiedName;
            this.typeVariables = typeVariables;
        }

        public static Result compound(String qualifiedName, ZenClassNode... typeVariables) {
            return new Result(qualifiedName, typeVariables);
        }

        public static Result single(ZenClassNode classNode) {
            return new Result(classNode.getQualifiedName(), classNode);
        }

        public static Result missing(ZenClassTree tree, Type originType) {
            ProbeZS.logger.warn("Failed to reflect {} to zenscript type", originType.getTypeName());
            return new MissingResult(tree);
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public ZenClassNode[] getTypeVariableArray() {
            return typeVariables;
        }

        public List<ZenClassNode> getTypeVariables() {
            return Arrays.asList(typeVariables);
        }
    }

    public static class MissingResult extends Result {

        private MissingResult(ZenClassTree tree) {
            super("any", tree.getAnyClass());
        }
    }

    public static class Serializer implements JsonSerializer<Supplier<Result>> {

        @Override
        public JsonElement serialize(Supplier<Result> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.get().getQualifiedName());
        }
    }

    public static class FullNameSerializer implements JsonSerializer<LazyZenClassNode> {

        @Override
        public JsonElement serialize(LazyZenClassNode src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.get().getTypeVariableArray()[0].getName());
        }
    }
}
