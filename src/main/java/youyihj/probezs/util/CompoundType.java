package youyihj.probezs.util;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class CompoundType implements Type {
    private final Type[] types;

    public CompoundType(Type[] types) {
        this.types = types;
    }

    public Type[] getCompoundTypes() {
        return types;
    }

    public CompoundType append(Type type) {
        return new CompoundType(ArrayUtils.add(types, type));
    }

    @Override
    public String toString() {
        return Arrays.stream(getCompoundTypes()).map(Objects::toString).collect(Collectors.joining(" | "));
    }
}
