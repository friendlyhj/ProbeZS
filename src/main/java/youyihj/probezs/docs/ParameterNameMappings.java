package youyihj.probezs.docs;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

public class ParameterNameMappings {
    private static Map<String, List<Map<String, Object>>> nameMappings;

    public static void load(String path) {
        try (InputStream inputStream = ParameterNameMappings.class.getClassLoader().getResourceAsStream(path)) {
            Yaml yaml = new Yaml();
            nameMappings = yaml.loadAs(inputStream, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> find(Method method) {
        if (nameMappings == null) {
            load("mappings/method-parameter-names.yaml");
        }
        String clazzName = method.getDeclaringClass().getCanonicalName();
        List<Map<String, Object>> datas = nameMappings.get(clazzName);

        if (method.getParameterTypes().length == 0) {
            return null;
        }
        if (datas == null) {
            return null;
        }
        String name = method.getName();
        StringJoiner joiner = new StringJoiner(",");
        for (Class<?> parameterType : method.getParameterTypes()) {
            joiner.add(parameterType.getCanonicalName());
        }
        for (Map<String, Object> data : datas) {
            if (Objects.equals(name, data.get("name")) && Objects.equals(joiner.toString(), data.get("paramsSignature"))) {
                List<String> result = (List<String>) data.get("paramNames");
                if (result.size() == method.getParameterCount()) {
                    return result;
                }
            }
        }
        return null;
    }
}
