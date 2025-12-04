package youyihj.probezs.docs;

import org.yaml.snakeyaml.Yaml;
import youyihj.probezs.ProbeZS;
import youyihj.zenutils.impl.member.ExecutableData;
import youyihj.zenutils.impl.member.TypeData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ParameterNameMappings {
    private Map<String, List<Map<String, Object>>> nameMappings;

    public void load(String path) {
        Yaml yaml = new Yaml();
        nameMappings = yaml.loadAs(ProbeZS.instance.mappingsFuture.join(), Map.class);
    }

    public List<String> find(ExecutableData method) {
        if (nameMappings == null) {
            load("mappings/method-parameter-names.yaml");
        }
        String clazzName = method.declaringClass().name().replace('$', '.');
        List<Map<String, Object>> datas = nameMappings.get(clazzName);

        if (method.parameterCount() == 0) {
            return null;
        }
        if (datas == null) {
            return null;
        }
        String name = method.name();
        StringJoiner joiner = new StringJoiner(",");
        for (TypeData parameterType : method.parameters()) {
            String descriptor = parameterType.descriptor();
            int arrayDim = 0;
            while (descriptor.startsWith("[")) {
                descriptor = descriptor.substring(1);
                arrayDim++;
            }
            if (descriptor.startsWith("I")) {
                descriptor = "int";
            } else if (descriptor.startsWith("B")) {
                descriptor = "byte";
            } else if (descriptor.startsWith("S")) {
                descriptor = "short";
            } else if (descriptor.startsWith("J")) {
                descriptor = "long";
            } else if (descriptor.startsWith("F")) {
                descriptor = "float";
            } else if (descriptor.startsWith("D")) {
                descriptor = "double";
            } else if (descriptor.startsWith("Z")) {
                descriptor = "boolean";
            } else if (descriptor.startsWith("C")) {
                descriptor = "char";
            } else if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                descriptor = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
            }
            StringBuilder typeNameBuilder = new StringBuilder(descriptor);
            for (int i = 0; i < arrayDim; i++) {
                typeNameBuilder.append("[]");
            }
            joiner.add(typeNameBuilder.toString());
        }
        for (Map<String, Object> data : datas) {
            if (Objects.equals(name, data.get("name")) && Objects.equals(joiner.toString(), data.get("paramsSignature"))) {
                List<String> result = (List<String>) data.get("paramNames");
                if (result.size() == method.parameterCount()) {
                    return result;
                }
            }
        }
        return null;
    }
}
