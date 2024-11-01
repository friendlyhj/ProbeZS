package youyihj.probezs.docs;

import org.yaml.snakeyaml.Yaml;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.member.ExecutableData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class ParameterNameMappings {
    private Map<String, List<Map<String, Object>>> nameMappings;

    public void load(String path) {
        Yaml yaml = new Yaml();
        CompletableFuture<String> mappingsFuture = ProbeZS.instance.mappingsFuture;
        try {
            if (!mappingsFuture.isDone() || mappingsFuture.isCompletedExceptionally()) {
                mappingsFuture.cancel(true);
                try (InputStream inputStream = ParameterNameMappings.class.getClassLoader().getResourceAsStream(path)) {
                    nameMappings = yaml.loadAs(inputStream, Map.class);
                }
            } else {
                nameMappings = yaml.loadAs(mappingsFuture.get(), Map.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> find(ExecutableData method) {
        if (nameMappings == null) {
            load("mappings/method-parameter-names.yaml");
        }
        String clazzName = method.getDecalredClass().getCanonicalName();
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
