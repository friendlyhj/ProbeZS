package youyihj.probezs.docs;

import org.yaml.snakeyaml.Yaml;
import youyihj.probezs.ProbeZS;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ParameterNameMappings {
    private static Map<String, List<Map<String, Object>>> nameMappings;

    public static void load(String path) {
        Yaml yaml = new Yaml();
        if (ProbeZS.mappings.isEmpty()) {
            try (InputStream inputStream = ParameterNameMappings.class.getClassLoader().getResourceAsStream(path)) {
                nameMappings = yaml.loadAs(inputStream, Map.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            nameMappings = yaml.loadAs(ProbeZS.mappings, Map.class);
        }
    }

    public static List<String> find(Executable method) {
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
        String name = method instanceof Constructor ? "<init>" : method.getName();
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
