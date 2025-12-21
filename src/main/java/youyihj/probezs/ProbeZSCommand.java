package youyihj.probezs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.teamacronymcoders.base.materialsystem.MaterialSystem;
import com.teamacronymcoders.base.materialsystem.materialparts.MaterialPart;
import com.teamacronymcoders.contenttweaker.api.ContentTweakerAPI;
import com.teamacronymcoders.contenttweaker.api.ctobjects.blockmaterial.IBlockMaterialDefinition;
import com.teamacronymcoders.contenttweaker.modules.materials.brackethandler.MaterialPartDefinition;
import com.teamacronymcoders.contenttweaker.modules.vanilla.resources.sounds.ISoundEventDefinition;
import com.teamacronymcoders.contenttweaker.modules.vanilla.resources.sounds.ISoundTypeDefinition;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.creativetabs.ICreativeTab;
import crafttweaker.api.damage.IDamageSource;
import crafttweaker.api.enchantments.IEnchantmentDefinition;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.api.potions.IPotion;
import crafttweaker.api.potions.IPotionType;
import crafttweaker.api.world.IBiome;
import crafttweaker.mc1120.brackets.BracketHandlerEnchantments;
import crafttweaker.mc1120.commands.CraftTweakerCommand;
import crafttweaker.mc1120.damage.expand.MCDamageSourceExpand;
import crafttweaker.mc1120.util.CraftTweakerHacks;
import crafttweaker.preprocessor.PreprocessorFactory;
import crafttweaker.zenscript.GlobalRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.command.ICommandSender;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import youyihj.probezs.bracket.BracketHandlerEntryProperties;
import youyihj.probezs.bracket.BracketHandlerMirror;
import youyihj.probezs.core.asm.CraftTweakerAPIHooks;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.global.ZenGlobalMemberTree;
import youyihj.probezs.util.FileUtils;
import youyihj.zenutils.Reference;
import youyihj.zenutils.impl.member.ClassData;
import youyihj.zenutils.impl.util.InternalUtils;
import youyihj.zenutils.impl.zenscript.nat.NativeClassValidate;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ProbeZSCommand extends CraftTweakerCommand {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<String> BLACKLIST = Lists.newArrayList(
            "java.io",
            "java.nio",
            "java.awt",
            "java.applet",
            "java.rmi",
            "java.net",
            "java.security",
            "java.util.zip",
            "java.util.concurrent",
            "java.util.logging",
            "scala",
            "kotlin",
            "stanhebben.zenscript",
            "org.apache.commons.io",
            "org.apache.http",
            "org.apache.logging",
            "io.netty",
            "org.spongepowered.",
            "com.llamalad7.mixinextras",
            "org.objectweb.asm",
            "sun.",
            "jdk",
            "javax",
            "groovy",
            "com.cleanroommc.groovyscript",
            "org.prismlauncher",
            "org.jackhuang.hmcl"
    );

    private static final Method TRANSFORM_NAME_METHOD;

    static {
        try {
            Class<?> lclClass = Reference.IS_CLEANROOM ? Class.forName("top.outlands.foundation.boot.ActualClassLoader") : LaunchClassLoader.class;
            TRANSFORM_NAME_METHOD = lclClass.getDeclaredMethod("transformName", String.class);
            TRANSFORM_NAME_METHOD.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to get transformName method", e);
        }
    }

    public ProbeZSCommand() {
        super("probezs");
    }

    @Override
    protected void init() {

    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        Path probeZSPath = getProbeZSPathFromIntellizenJson();
        if (probeZSPath == null) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Can not read dump dir from intellizen.json, generate one now"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Please define the path of dzs scripts by executing " + TextFormatting.GREEN + "/ct probezs minecraft" + TextFormatting.AQUA + " or " + TextFormatting.GREEN + "/ct probezs user"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "minecraft: this minecraft instance dir"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "user: USER_DIR/.probezs"));
            } else {
                probeZSPath = generateDefaultIntellizenJson(args[0], sender);
            }
        } else {
            if (args.length > 0) {
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Warning: dump dir is already defined by intellizen.json, the argument will be ignored"));
            }
        }
        if (probeZSPath != null) {
            Path finalProbeZSPath = probeZSPath;
            sender.sendMessage(new TextComponentString("Dumping dzs to " + finalProbeZSPath.toAbsolutePath()));
            CompletableFuture.runAsync(() -> run(finalProbeZSPath, sender))
                    .exceptionally(throwable -> {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Can not dump dzs lib " + throwable));
                        return null;
                    })
                    .thenAccept(aVoid -> sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Dumped dzs to " + finalProbeZSPath.toAbsolutePath())));
        }
    }

    @Override
    public List<String> getSubSubCommand(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Arrays.asList("minecraft", "user");
    }

    private void run(Path probeZSPath, ICommandSender sender) {
        try {
            if (Files.exists(probeZSPath)) {
                removeOldScripts(probeZSPath);
            } else {
                Files.createDirectories(probeZSPath);
            }
            Files.createDirectories(probeZSPath);
            ZenClassTree tree = new ZenClassTree(CraftTweakerAPIHooks.ZEN_CLASSES);
            if (ProbeZSConfig.dumpNativeMembers) {
                dumpNativeClasses(tree, sender);
            }
            ZenGlobalMemberTree globalMemberTree = dumpGlobalMembers(tree);
            List<BracketHandlerMirror> mirrors = dumpBracketHandlerMirrors(tree);
            tree.fresh();
            tree.output(probeZSPath);
            globalMemberTree.output(probeZSPath);
            outputBracketHandlerMirrors(mirrors, probeZSPath);
            outputPreprocessors(probeZSPath);
            dumpEnvironment(probeZSPath);
        } catch (Exception e) {
            ProbeZS.logger.error("Can not dump zs lib ", e);
            throw new RuntimeException(e);
        }
    }

    private Path getProbeZSPathFromIntellizenJson() {
        Path path = FileSystems.getDefault().getPath("intellizen.json");
        if (!Files.exists(path)) {
            path = FileSystems.getDefault().getPath("scripts", "probezs.json");
        }
        try {
            JsonObject json = GSON.fromJson(Files.newBufferedReader(path), JsonElement.class).getAsJsonObject();
            String probeZSPath = json.getAsJsonObject("probezs")
                    .get("dumpDir")
                    .getAsString();
            Path parent = path.getParent();
            return parent == null ? FileSystems.getDefault().getPath(probeZSPath) : parent.resolve(probeZSPath);
        } catch (Exception ignored) {
        }
        return null;
    }

    private Path generateDefaultIntellizenJson(String arg, ICommandSender sender) {
        Path intellizenPath = FileSystems.getDefault().getPath("intellizen.json");
        String probeZSPath;
        switch (arg) {
            case "minecraft":
                probeZSPath = "./probezs";
                break;
            case "user":
                try {
                    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                    Path scriptsPath;
                    try {
                        scriptsPath = FileSystems.getDefault().getPath("scripts").toRealPath();
                    } catch (IOException e) {
                        scriptsPath = FileSystems.getDefault()
                                .getPath(System.getProperty("user.dir"))
                                .resolve("scripts");
                    }
                    Environment.put("scriptPath", scriptsPath.toString());
                    sha1.update(scriptsPath.toString().getBytes(StandardCharsets.UTF_8));
                    probeZSPath = System.getProperty("user.home") + "/.probezs/" + Hex.encodeHexString(sha1.digest());
                    probeZSPath = probeZSPath.replace('\\', '/');
                } catch (NoSuchAlgorithmException e) { // really?
                    throw new AssertionError();
                }
                break;
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown dir argument"));
                return null;
        }
        JsonObject json;
        try (BufferedReader reader = Files.newBufferedReader(intellizenPath, StandardCharsets.UTF_8)) {
            json = GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            json = new JsonObject();
        }
        Set<JsonElement> srcRoots;
        if (json.has("srcRoots")) {
            srcRoots = Sets.newHashSet(json.get("srcRoots").getAsJsonArray());
        } else {
            srcRoots = Sets.newHashSet();
        }
        srcRoots.add(new JsonPrimitive("./scripts"));
        JsonObject dumpDirRef = new JsonObject();
        dumpDirRef.addProperty("$ref", "#/probezs/dumpDir");
        srcRoots.add(dumpDirRef);
//        srcRoots.add(new JsonPrimitive(probeZSPath));
        json.add("srcRoots", newJsonArray(srcRoots));
        JsonObject probezs = new JsonObject();
        probezs.addProperty("dumpDir", probeZSPath);
        probezs.addProperty("port", 6489);
        json.add("probezs", probezs);
        try (BufferedWriter writer = Files.newBufferedWriter(intellizenPath)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to write intellizen.json file"));
        }
        return FileSystems.getDefault().getPath(probeZSPath);
    }

    private static JsonArray newJsonArray(Collection<JsonElement> elements) {
        JsonArray array = new JsonArray();
        for (JsonElement element : elements) {
            array.add(element);
        }
        return array;
    }

    private void removeOldScripts(Path dzsPath) {
        try {
            Files.walkFileTree(dzsPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileString = file.toString();
                    if (fileString.endsWith(".json") || fileString.endsWith(".dzs")) {
                        if (!file.getParent().equals(dzsPath)) {
                            Files.delete(file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to remove old scripts", e);
        }
    }

    private ZenGlobalMemberTree dumpGlobalMembers(ZenClassTree classTree) {
        ZenGlobalMemberTree globalMemberTree = new ZenGlobalMemberTree(classTree);
        globalMemberTree.readGlobals(GlobalRegistry.getGlobals());
        return globalMemberTree;
    }

    private List<BracketHandlerMirror> dumpBracketHandlerMirrors(ZenClassTree classTree) {
        List<BracketHandlerMirror> mirrors = Lists.newArrayList(
                BracketHandlerMirror.<IItemStack>builder(classTree)
                        .setType(IItemStack.class)
                        .setEntries(ProbeZS.safeGetItemRegistry())
                        .setRegex("^item:.*")
                        .setIdMapper(it -> {
                            String commandString = it.toCommandString();
                            return commandString.substring(1, commandString.length() - 1);
                        })
                        .setPropertiesAdder((item, properties) -> {
                            properties.add("name", ProbeZS.safeGetItemName(item), true);
                        })
                        .build(),
                BracketHandlerMirror.<Block>builder(classTree)
                        .setType(IBlockState.class)
                        .setEntries(ForgeRegistries.BLOCKS.getValuesCollection())
                        .setRegex("^blockstate:.*")
                        .setIdMapper(it -> "blockstate:" + it.getRegistryName())
                        .setPropertiesAdder(this::fillBlockProperties)
                        .build(),
                BracketHandlerMirror.<IIngredient>builder(classTree)
                        .setRegex("^\\*$")
                        .setType(IIngredient.class)
                        .setEntries(Collections.singleton(IngredientAny.INSTANCE))
                        .setIdMapper(it -> "*")
                        .build(),
                BracketHandlerMirror.<String>builder(classTree)
                        .setType(ILiquidStack.class)
                        .setRegex("^(fluid|liquid):.*")
                        .setEntries(FluidRegistry.getRegisteredFluids().keySet())
                        .setIdMapper(it -> "liquid:" + it.replace(" ", ""))
                        .setPropertiesAdder((liquid, properties) -> {
                            FluidStack fluidStack = Objects.requireNonNull(FluidRegistry.getFluidStack(liquid, Fluid.BUCKET_VOLUME));
                            properties.add("name", fluidStack.getLocalizedName(), true);
                        })
                        .build(),
                BracketHandlerMirror.<IBiome>builder(classTree)
                        .setType(IBiome.class)
                        .setRegex("^biome:.*")
                        .setEntries(CraftTweakerAPI.game.getBiomes())
                        .setIdMapper(it -> "biome:" + it.getId().split(":")[1])
                        .build(),
                BracketHandlerMirror.<String>builder(classTree)
                        .setType(ICreativeTab.class)
                        .setRegex("^creativetab:.*")
                        .setEntries(CraftTweakerMC.creativeTabs.keySet())
                        .setIdMapper("creativetab:"::concat)
                        .build(),
                BracketHandlerMirror.<Method>builder(classTree)
                        .setType(IDamageSource.class)
                        .setRegex("^damageSource:.*")
                        .setEntries(Arrays.stream(MCDamageSourceExpand.class.getDeclaredMethods())
                                .filter(it -> it.getParameterCount() == 0)
                                .collect(Collectors.toList())
                        )
                        .setIdMapper(it -> "damageSource:" + it.getName())
                        .build(),
                BracketHandlerMirror.<String>builder(classTree)
                        .setType(IEnchantmentDefinition.class)
                        .setRegex("^enchantment:.*")
                        .setEntries(BracketHandlerEnchantments.enchantments.keySet())
                        .setIdMapper(it -> "enchantment:" + it)
                        .build(),
                BracketHandlerMirror.<IEntityDefinition>builder(classTree)
                        .setType(IEntityDefinition.class)
                        .setRegex("^entity:.*")
                        .setEntries(CraftTweakerAPI.game.getEntities())
                        .setIdMapper(it -> "entity:" + it.getId())
                        .build(),
                BracketHandlerMirror.<IOreDictEntry>builder(classTree)
                        .setType(IOreDictEntry.class)
                        .setRegex("^ore:.*")
                        .setEntries(CraftTweakerAPI.oreDict.getEntries())
                        .setIdMapper(it -> "ore:" + it.getName())
                        .setPropertiesAdder((od, properties) -> {
                            properties.add("name", ProbeZS.safeGetItemName(od.getFirstItem()), true);
                        })
                        .build(),
                BracketHandlerMirror.<Potion>builder(classTree)
                        .setType(IPotion.class)
                        .setRegex("^potion:.*")
                        .setEntries(ForgeRegistries.POTIONS.getValuesCollection())
                        .setIdMapper(it -> "potion:" + it.getRegistryName())
                        .build(),
                BracketHandlerMirror.<PotionType>builder(classTree)
                        .setType(IPotionType.class)
                        .setRegex("^potiontype:.*")
                        .setEntries(ForgeRegistries.POTION_TYPES.getValuesCollection())
                        .setIdMapper(it -> "potiontype:" + it.getRegistryName())
                        .build()
        );
        if (Loader.isModLoaded("contenttweaker")) {
            mirrors.addAll(ContentTweaker.dumpCoTBracketMirrors(classTree));
        }

        return mirrors;
    }

    private void outputPreprocessors(Path dzsPath) {
        HashMap<String, PreprocessorFactory<?>> registeredPreprocessorActions = CraftTweakerHacks.getPrivateObject(CraftTweakerAPI.tweaker.getPreprocessorManager(), "registeredPreprocessorActions");
        try {
            FileUtils.createFile(
                    dzsPath.resolve("preprocessors.json"),
                    registeredPreprocessorActions.keySet()
                            .stream()
                            .map(it -> StringUtils.wrap(it, "\""))
                            .collect(Collectors.toList())
                            .toString()
            );
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to output preprocessor json", e);
        }
    }

    private void outputBracketHandlerMirrors(List<BracketHandlerMirror> mirrors, Path dzsPath) {
        String json = BracketHandlerMirror.GSON.toJson(mirrors);
        try {
            FileUtils.createFile(dzsPath.resolve("brackets.json"), json);
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to output brackets json", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> void fillBlockProperties(Block block, BracketHandlerEntryProperties properties) {
        for (IProperty<?> property : block.getBlockState().getProperties()) {
            IProperty<T> propertyT = ((IProperty<T>) property);
            List<String> values = new ArrayList<>();
            for (T allowedValue : propertyT.getAllowedValues()) {
                values.add(propertyT.getName(allowedValue));
            }
            properties.add(property.getName(), values, false);
        }
    }

    private void dumpNativeClasses(ZenClassTree tree, ICommandSender sender) {
        List<URL> urls = Lists.newArrayList(Launch.classLoader.getURLs()).stream().filter(it -> it.toString().endsWith(".jar")).collect(Collectors.toList());
        // java 8
        if (!Reference.IS_CLEANROOM) {
            try {
                urls.add(new URL(("file:/" + System.getProperty("java.home") + "/lib/rt.jar").replace(" ", "%20")));
            } catch (MalformedURLException e) {
                throw new RuntimeException("jdk path is invalid");
            }
        }
        for (int i = 0; i < urls.size(); i++) {
            URL url = urls.get(i);
            notifySenderAsync(sender, "Dumping native classes from jar " + url + " (" + (i + 1) + "/" + urls.size() + ")");
            try (FileSystem jarFs = FileSystems.newFileSystem(URI.create("jar:" + url.toURI().toASCIIString()), Collections.emptyMap())) {
                readJarFile(jarFs, tree);
            } catch (Exception e) {
                ProbeZS.logger.warn("Failed to read jar file {}", url, e);
            }
        }
        // java 9+
        if (Reference.IS_CLEANROOM) {
            notifySenderAsync(sender, "Dumping jdk classes");
            try {
                FileSystem jrtFs = FileSystems.getFileSystem(URI.create("jrt:/"));
                readJarFile(jrtFs, tree);
            } catch (Exception e) {
                ProbeZS.logger.warn("Failed to read jrt file system", e);
            }
        }
    }

    private void readJarFile(FileSystem jarFs, ZenClassTree tree) throws IOException {
        Files.walkFileTree(jarFs.getPath("/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".class")) {
                    String className = file.toString()
                            .substring(1, file.toString().length() - 6)
                            .replace('/', '.');
                    try {
                        className = TRANSFORM_NAME_METHOD.invoke(Launch.classLoader, className).toString();
                        if (BLACKLIST.stream().anyMatch(className::startsWith)) {
                            return FileVisitResult.CONTINUE;
                        }
                        ClassData classData = InternalUtils.getClassDataFetcher().forName(className);
                        if (NativeClassValidate.isValid(classData)) {
                            tree.putNativeClass(classData);
                        }
                    } catch (Throwable e) {
                        ProbeZS.logger.warn("Failed to read native class {}", className);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void dumpEnvironment(Path dzsPath) {
//        Environment.put("probezsSocketPort", String.valueOf(ProbeZSConfig.socketPort));
//        Environment.put("probezsVersion", ProbeZS.VERSION);
//        CrashReportCategory category = new CrashReport("", new Throwable()).getCategory();
//        Map<String, CrashReportCategory.Entry> entries = category.children.stream()
//                .collect(Collectors.toMap(CrashReportCategory.Entry::getKey, Function.identity()));
//        Environment.put("operatingSystem", entries.get("Operating System").getValue());
//        Environment.put("javaVersion", entries.get("Java Version").getValue());
//        Environment.put("javaPath", System.getProperty("java.home"));
//        Environment.put("jvmVersion", entries.get("Java VM Version").getValue());
//        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
//        Environment.put("jvmFlags", runtimemxbean.getInputArguments());
//        Environment.put("classpath", runtimemxbean.getClassPath());
//        try {
//            Environment.put("bootClassPath", runtimemxbean.getBootClassPath());
//        } catch (UnsupportedOperationException e) {
//            Environment.put("bootClassPath", "");
//        }
//        Environment.output(dzsPath.resolve("env.json"));
    }

    private void notifySenderAsync(ICommandSender sender, String message) {
        sender.getServer().addScheduledTask(() -> sender.sendMessage(new TextComponentString(message)));
    }

    private static class ContentTweaker {

        @Optional.Method(modid = "contenttweaker")
        private static List<BracketHandlerMirror> dumpCoTBracketMirrors(ZenClassTree tree) {
            return Lists.newArrayList(
                    BracketHandlerMirror.<String>builder(tree)
                            .setType(IBlockMaterialDefinition.class)
                            .setRegex("^blockmaterial:.*")
                            .setEntries(ContentTweakerAPI.getInstance().getBlockMaterials().getAllNames())
                            .setIdMapper("blockmaterial:"::concat)
                            .build(),
                    BracketHandlerMirror.<String>builder(tree)
                            .setRegex("^soundtype:.*")
                            .setType(ISoundTypeDefinition.class)
                            .setEntries(ContentTweakerAPI.getInstance().getSoundTypes().getAllNames())
                            .setIdMapper("soundtype:"::concat)
                            .build(),
                    BracketHandlerMirror.<String>builder(tree)
                            .setRegex("^soundevent:.*")
                            .setType(ISoundEventDefinition.class)
                            .setEntries(ContentTweakerAPI.getInstance().getSoundEvents().getAllNames())
                            .setIdMapper("soundevent:"::concat)
                            .build(),
                    BracketHandlerMirror.<MaterialPart>builder(tree)
                            .setRegex("^materialpart:.*")
                            .setType(MaterialPartDefinition.class)
                            .setEntries(MaterialSystem.getMaterialParts().values())
                            .setIdMapper(it -> "materialpart:" + it.getMaterial()
                                    .getUnlocalizedName() + ":" + it.getPart()
                                    .getShortUnlocalizedName())
                            .build()
            );
        }
    }
}
