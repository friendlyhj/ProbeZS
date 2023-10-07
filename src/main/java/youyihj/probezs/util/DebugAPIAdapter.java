package youyihj.probezs.util;

import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenProperty;
import stanhebben.zenscript.util.ZenTypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class DebugAPIAdapter {


    public static void init() {
        // DO nothing, only make sure that class is loaded
    }

    public static Object[] iterableToArray(Iterable<?> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toArray();
    }

    public static String[] memberSignatures(Class<?> clazz) {
        List<String> result = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            Optional<ZenProperty> annotation = Arrays.stream(field.getAnnotations()).filter(it -> it instanceof ZenProperty).map(it -> (ZenProperty) it).findAny();
            if (!annotation.isPresent()) {
                continue;
            }
            String propertyName = annotation.get().value();
            if (propertyName.isEmpty()) {
                propertyName = field.getName();
            }
            result.add(propertyName + ":" + field.getName());
        }

        for (Method method : clazz.getMethods()) {
            Optional<ZenGetter> annotation = Arrays.stream(method.getAnnotations()).filter(it -> it instanceof ZenGetter).map(it -> (ZenGetter) it).findAny();
            if (!annotation.isPresent()) {
                continue;
            }

            String propertyName = annotation.get().value();
            if (propertyName.isEmpty()) {
                propertyName = method.getName();
            }
            result.add(propertyName + ":" + method.getName() + ":" + ZenTypeUtil.descriptor(method));

        }

        return result.toArray(new String[0]);
    }


}
