package youyihj.probezs.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class IntersectionType implements Type {
    private final Type[] types;

    public IntersectionType(Type[] types) {
        this.types = types;
    }

    public Type[] getCompoundTypes() {
        return types;
    }

    @Override
    public String toString() {
        return Arrays.stream(getCompoundTypes()).map(Objects::toString).collect(Collectors.joining(" & "));
    }
}
