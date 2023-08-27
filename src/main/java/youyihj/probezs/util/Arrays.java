package youyihj.probezs.util;

import java.lang.reflect.Array;
import java.util.function.Function;

/**
 * @author youyihj
 */
public class Arrays {
    public static <T, U> U[] map(T[] array, Class<U> resultClass, Function<T, U> mapper) {
        @SuppressWarnings("unchecked")
        U[] result = ((U[]) Array.newInstance(resultClass, array.length));
        for (int i = 0; i < array.length; i++) {
            result[i] = mapper.apply(array[i]);
        }
        return result;
    }
}
