package youyihj.probezs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import youyihj.probezs.member.asm.ASMMemberFactory;
import youyihj.probezs.member.asm.TypeResolver;

import java.util.Collections;
import java.util.List;

/**
 * @author youyihj
 */
public class TypeResolverTest {
    private TypeResolver typeResolver;

    private static final String OBJECT = "Ljava/lang/Object;";
    private static final String LIST = "Ljava/util/List<Ljava/lang/Object;>;";
    private static final String MAP = "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;";
    private static final String INT = "I";
    private static final String INT_ARRAY = "[I";
    private static final String MAP_ARRAY = "[" + MAP;
    private static final String LIST_ELEMENT_ARRAY = "Ljava/util/List<[I>;";
    private static final String LIST_GENERIC = "Ljava/util/List<TE;>;";
    private static final String GENERIC = "TF;";

    public static String buildMethodSignature(String... paramSignature) {
        StringBuilder sb = new StringBuilder("(");
        for (String s : paramSignature) {
            sb.append(s);
        }
        return sb.append(")V").toString();
    }

    public void assertResolve(String... paramSignature) {
        List<String> result = typeResolver.resolveMethodArguments(buildMethodSignature(paramSignature));
        Assertions.assertArrayEquals(result.toArray(new String[0]), paramSignature);
    }

    @BeforeEach
    public void setupTypeResolver() {
        typeResolver = new TypeResolver(new ASMMemberFactory(Collections.emptySet(), () -> TypeResolverTest.class::getClassLoader));
    }

    @Test
    public void allPrimitiveTypes() {
        assertResolve("I", "I", "I", "Z", "Z", "F", "D");
    }

    @Test
    public void compoundTypes() {
        assertResolve(OBJECT, LIST, MAP);
    }

    @Test
    public void primitiveAndCompoundTypes() {
        assertResolve(INT, OBJECT, INT, LIST, INT, MAP);
    }

    @Test
    public void primitiveArrays() {
        assertResolve(INT_ARRAY, INT_ARRAY, INT);
    }

    @Test
    public void primitiveArraysAndCompoundType() {
        assertResolve(INT_ARRAY, OBJECT, INT_ARRAY, MAP);
    }

    @Test
    public void compoundTypeArray() {
        assertResolve(MAP_ARRAY, INT, INT_ARRAY);
    }

    @Test
    public void compoundListElement() {
        assertResolve(MAP_ARRAY, LIST_ELEMENT_ARRAY, INT, INT_ARRAY);
    }

    @Test
    public void genericType() {
        assertResolve(GENERIC, LIST_GENERIC);
    }

}
