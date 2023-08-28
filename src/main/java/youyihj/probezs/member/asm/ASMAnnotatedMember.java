package youyihj.probezs.member.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import youyihj.probezs.member.AnnotatedMember;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;

/**
 * @author youyihj
 */
public class ASMAnnotatedMember implements AnnotatedMember {
    private final List<AnnotationNode> annotationNodes;
    protected final ASMMemberFactory memberFactory;

    public ASMAnnotatedMember(List<AnnotationNode> annotationNodes, ASMMemberFactory memberFactory) {
        this.annotationNodes = annotationNodes;
        this.memberFactory = memberFactory;
    }

    @Override
    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        if (annotationNodes != null) {
            for (AnnotationNode annotation : annotationNodes) {
                if (Objects.equals(annotation.desc, org.objectweb.asm.Type.getDescriptor(annotationClass))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        if (annotationNodes != null) {
            for (AnnotationNode annotation : annotationNodes) {
                if (Objects.equals(annotation.desc, org.objectweb.asm.Type.getDescriptor(annotationClass))) {
                    return (A) Proxy.newProxyInstance(memberFactory.getClassLoader(), new Class[]{annotationClass}, ((proxy, method, args) -> {
                        List<Object> values = annotation.values;
                        if (values != null) {
                            for (int i = 0; i < values.size(); i += 2) {
                                if (Objects.equals(values.get(i), method.getName())) {
                                    return parseAnnotationValue(values.get(i + 1));
                                }
                            }
                        }
                        if (method.getName().equals("annotationType")) {
                            return annotationClass;
                        }
                        return method.getDefaultValue();
                    }));
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parseAnnotationValue(Object value) throws Exception {
        if (value.getClass() == Type.class) {
            return memberFactory.getTypeDescResolver().convertASMType(((Type) value));
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            Object[] values = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                values[i] = parseAnnotationValue(list.get(i));
            }
            return values;
        }
        if (value instanceof String[]) {
            String[] strings = (String[]) value;
            return Enum.valueOf(((Class<Enum>) Class.forName(Type.getType(strings[0]).getClassName(), true, memberFactory.getClassLoader())), strings[1]);
        }
        return value;
    }
}
