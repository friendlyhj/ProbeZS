package youyihj.probezs.tree;

import crafttweaker.util.IEventHandler;
import org.apache.commons.lang3.ArrayUtils;
import youyihj.probezs.ProbeZS;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        existed = !result.getQualifiedName().equals("any");
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
                    ZenClassNode nativeClass = javaMap.computeIfAbsent(((Class<?>) type), it -> {
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
                    return nativeClass == null ? Result.single(classTree.getAnyClass()) : Result.single(nativeClass);
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
            }
        } catch (Exception e) {
            ProbeZS.logger.error("Failed to reflect {} to zenscript type", type.getTypeName(), e);
        }
        return Result.single(classTree.getAnyClass());
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
}
