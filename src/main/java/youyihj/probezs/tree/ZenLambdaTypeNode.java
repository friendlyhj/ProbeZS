package youyihj.probezs.tree;

import youyihj.probezs.util.IndentStringBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ZenLambdaTypeNode implements IZenDumpable {
    private final List<LazyZenClassNode> args;
    private final LazyZenClassNode returnType;

    public ZenLambdaTypeNode(List<LazyZenClassNode> args, LazyZenClassNode returnType) {
        this.args = args;
        this.returnType = returnType;
    }

    public static ZenLambdaTypeNode read(Class<?> clazz, ZenClassTree tree) {
        if (clazz.isInterface()) {
            Method lambdaMethod = null;
            for (Method method : clazz.getMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isAbstract(modifiers)) {
                    if (lambdaMethod != null) {
                        return null;
                    }
                    lambdaMethod = method;
                }
            }
            if (lambdaMethod != null) {
                List<LazyZenClassNode> args = Arrays.stream(lambdaMethod.getGenericParameterTypes())
                        .map(tree::createLazyClassNode)
                        .collect(Collectors.toList());
                LazyZenClassNode returnType = tree.createLazyClassNode(lambdaMethod.getGenericReturnType());
                return new ZenLambdaTypeNode(args, returnType);
            }
        }
        return null;
    }

    @Override
    public void toZenScript(IndentStringBuilder sb) {
        Iterator<LazyZenClassNode> iterator = args.iterator();
        sb.append("function(");
        while (iterator.hasNext()) {
            sb.append(iterator.next().get().getName());
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append(")").append(returnType.get().getName());
    }
}
