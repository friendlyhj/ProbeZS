package youyihj.probezs.generator;

import org.apache.commons.compress.utils.Sets;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;
import stanhebben.zenscript.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class MethodParameterNamesGenerator {

    private static final Set<String> extras = Sets.newHashSet(
        "crafttweaker.runtime.GlobalFunctions::print",
        "crafttweaker.runtime.GlobalFunctions::totalActions",
        "crafttweaker.runtime.GlobalFunctions::enableDebug",
        "crafttweaker.runtime.GlobalFunctions::isNull"
    );


    public static void main(String[] args) {
        String folderPath = args[0];
        String outputPath = args[1];

        Launcher launcher = new Launcher();
        launcher.addInputResource(folderPath);
        launcher.buildModel();
        CtModel model = launcher.getModel();

        Filter<CtMethod<?>> filter = new CompositeFilter<>(
            FilteringOperator.INTERSECTION,
            new TypeFilter<>(CtMethod.class),
            new CompositeFilter<>(
                FilteringOperator.UNION,
                new AnnotationFilter<>(ZenMethod.class),
                new AnnotationFilter<>(ZenMethodStatic.class),
                new AnnotationFilter<>(ZenCaster.class),
                new AnnotationFilter<>(ZenOperator.class),
                new AnnotationFilter<>(ZenConstructor.class)
            )
        );
        List<CtMethod<?>> methods = new ArrayList<>(model.getElements(filter));

        // add globals
        addExtras(model, methods);
        Map<String, List<Map<String, Object>>> result = generateSource(methods);
        addDefaults(result);
        DumperOptions options = new DumperOptions();
        options.setAllowReadOnlyProperties(true);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try {
            File file = new File(outputPath + "/method-parameter-names.yaml");
            file.getParentFile().mkdirs();
            yaml.dump(result, new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDefaults(Map<String, List<Map<String, Object>>> result) {
        try (InputStream inputStream = MethodParameterNamesGenerator.class.getClassLoader().getResourceAsStream("default.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, List<Map<String, Object>>> map = yaml.loadAs(inputStream, Map.class);
            result.putAll(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void addExtras(CtModel model, List<CtMethod<?>> methods) {
        // functional interface
        model.getElements(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtInterface.class),
                new AnnotationFilter<>(ZenClass.class)
            )).stream().map(
                it -> {
                    Set<CtMethod<?>> ele = it.getAllMethods();
                    return ele.stream()
                        .filter(m -> !m.isDefaultMethod())
                        .filter(CtModifiable::isPublic)
                        .filter(CtModifiable::isAbstract)
                        .collect(Collectors.toList());
                }
            )
            .filter(it -> it.size() == 1)
            .map(it -> it.get(0))
            .forEach(methods::add);

        // manuals
        model.getElements(new TypeFilter<>(CtMethod.class))
            .stream().filter(
                it -> {
                    CtType<?> declaringType = it.getDeclaringType();
                    if (declaringType == null) {
                        return false;
                    }
                    String fullName = declaringType.getQualifiedName() + "::" + it.getSimpleName();
                    return extras.contains(fullName);
                }
            ).forEach(methods::add);
    }


    private static Map<String, List<Map<String, Object>>> generateSource(List<CtMethod<?>> methods) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        // Process each method
        for (CtMethod<?> method : methods) {
            String clazzName = method.getDeclaringType().getQualifiedName();
            String methodName = method.getSimpleName();
            List<String> parameterNames = new ArrayList<>();
            List<String> parameterSignatures = new ArrayList<>();


            if (method.getParameters().isEmpty()) {
                continue;
            }
            for (CtParameter<?> parameter : method.getParameters()) {
                parameterNames.add(parameter.getSimpleName());
                parameterSignatures.add(parameter.getType().getQualifiedName());
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", methodName);
            data.put("paramsSignature", String.join(",", parameterSignatures));
            data.put("paramNames", parameterNames);

            result.computeIfAbsent(clazzName, it -> new ArrayList<>())
                .add(data);

        }

        return result;
    }


}
