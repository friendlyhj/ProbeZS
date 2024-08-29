# ProbeZS

Dumps all ZenScript classes, methods and vanilla bracket handler entries into `*.dzs` files.

It only targets at MC 1.12.2. And there are no plans to update to modern versions.

Works with [ZenScript IntelliSense](https://marketplace.visualstudio.com/items?itemName=raylras.zenscript-intelli-sense)

## Code Generators

Since Java compiled bytecodes don't retain parameter names by default. ProbeZS keeps a parameter name mapping by analysing source code.

If you want to dump parameter names of another mod, please make a PR to add a submodule link the mod repo in ModSources directory.

* You can add extra entries manually in `src/generator/resources/default.yaml`
* You can add methods that aren't annotated `@ZenMethod` in `MethodParameterNamesGenerator.extras`
