package youyihj.probezs.docs;

import org.yaml.snakeyaml.Yaml;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.member.ExecutableData;
import youyihj.probezs.util.LoadingObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ParameterNameMappings {
    private static LoadingObject<Map<String, List<Map<String, Object>>>> nameMappings;

    public static void load(String path) {
        Yaml yaml = new Yaml();
        if (ProbeZS.instance.mappings.get().isEmpty()) {
            try (InputStream inputStream = ParameterNameMappings.class.getClassLoader().getResourceAsStream(path)) {
                nameMappings = LoadingObject.of(yaml.loadAs(inputStream, Map.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            nameMappings = LoadingObject.of(yaml.loadAs(ProbeZS.instance.mappings.get(), Map.class));
        }
    }

    public static List<String> find(ExecutableData method) {
        if (nameMappings == null) {
            load("mappings/method-parameter-names.yaml");
        }
        String clazzName = method.getDecalredClass().getCanonicalName();
        List<Map<String, Object>> datas = nameMappings.get().get(clazzName);

        if (method.getParameterTypes().length == 0) {
            return null;
        }
        if (datas == null) {
            return null;
        }
        String name = method.getName();
        StringJoiner joiner = new StringJoiner(",");
        for (Class<?> parameterType : method.getParameterTypes()) {
            joiner.add(parameterType.getTypeName());
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
