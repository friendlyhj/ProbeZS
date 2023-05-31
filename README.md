## ProbeZS


### Code Generators

方法参数名称导出步骤
1. 将需要支持的模组源代码分支放入 ModSources 文件夹
2. 运行 gradlew generateParameterNameMappings
3. 将 generated 文件夹中的 method-parameter-names.yaml 复制到 resources 目录

可以在 src/generator/resources/default.yaml 中手动添加额外项
可以在 MethodParameterNamesGenerator.extras 中添加没有被 @ZenMethod 标记的方法