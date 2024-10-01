package youyihj.probezs;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
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

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ProbeZSCommand extends CraftTweakerCommand {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ProbeZSCommand() {
        super("probezs");
    }

    @Override
    protected void init() {

    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        Path dzsPath = getDZSPathFromIntellizenJson();
        if (dzsPath == null) {
            if (args.length == 0) {
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Can not read intellizen.json, generate one now"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Please define the path of dzs scripts by executing " + TextFormatting.GREEN + "/ct probezs minecraft" + TextFormatting.AQUA + " or " + TextFormatting.GREEN + "/ct probezs user"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "minecraft: this minecraft instance dir"));
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "user: USER_DIR/.probezs"));
            } else {
                dzsPath = generateDefaultIntellizenJson(args[0], sender);
            }
        }
        if (dzsPath != null) {
            try {
                if (Files.exists(dzsPath)) {
                    removeOldScripts(dzsPath);
                } else {
                    Files.createDirectories(dzsPath);
                }
                Files.createDirectories(dzsPath);
                ZenClassTree tree = new ZenClassTree(CraftTweakerAPIHooks.ZEN_CLASSES);
                ZenGlobalMemberTree globalMemberTree = dumpGlobalMembers(tree);
                List<BracketHandlerMirror> mirrors = dumpBracketHandlerMirrors(tree);
                tree.fresh();
                tree.output(dzsPath);
                globalMemberTree.output(dzsPath);
                outputBracketHandlerMirrors(mirrors, dzsPath);
                outputPreprocessors(dzsPath);
                dumpEnvironment(dzsPath);
            } catch (IOException e) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Can not dump zs lib " + e));
            }
            sender.sendMessage(new TextComponentString("Dump dzs successfully!"));
        }
    }

    @Override
    public List<String> getSubSubCommand(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Arrays.asList("minecraft", "user");
    }

    private Path getDZSPathFromIntellizenJson() {
        Path path = FileSystems.getDefault().getPath("intellizen.json");
        if (!Files.exists(path)) {
            path = FileSystems.getDefault().getPath("scripts", "probezs.json");
        }
        try {
            JsonObject json = GSON.fromJson(Files.newBufferedReader(path), JsonElement.class).getAsJsonObject();
            String dzsScriptsPath = json.get("dzs_scripts").getAsString();
            Path parent = path.getParent();
            return parent == null ? FileSystems.getDefault().getPath(dzsScriptsPath) : parent.resolve(dzsScriptsPath);
        } catch (Exception e) {
            return null;
        }
    }

    private Path generateDefaultIntellizenJson(String arg, ICommandSender sender) {
        Path intellizenPath = FileSystems.getDefault().getPath("intellizen.json");
        String dzsScriptsPath;
        switch (arg) {
            case "minecraft":
                dzsScriptsPath = "./dzs_scripts";
                break;
            case "user":
                try {
                    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                    Path scriptsPath;
                    try {
                        scriptsPath = FileSystems.getDefault().getPath("scripts").toRealPath();
                    } catch (
                            IOException e) {
                        scriptsPath = FileSystems.getDefault().getPath(System.getProperty("user.dir")).resolve("scripts");
                    }
                    Environment.put("scriptPath", scriptsPath.toString());
                    sha1.update(scriptsPath.toString().getBytes(StandardCharsets.UTF_8));
                    dzsScriptsPath = System.getProperty("user.home") + "/.probezs/" + Hex.encodeHexString(sha1.digest()) + "/dzs_scripts";
                    dzsScriptsPath = dzsScriptsPath.replace('\\', '/');
                } catch (NoSuchAlgorithmException e) { // really?
                    throw new AssertionError();
                }
                break;
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown dir argument"));
                return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("scripts", "./scripts");
        json.addProperty("dzs_scripts", dzsScriptsPath);
        try (BufferedWriter writer = Files.newBufferedWriter(intellizenPath)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to write intellizen.json file"));
        }
        return FileSystems.getDefault().getPath(dzsScriptsPath);
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
                                    .setRegex(".*")
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
                                    .setRegex("blockstate:.*")
                                    .setIdMapper(it -> "blockstate:" + it.getRegistryName())
                                    .setPropertiesAdder(this::fillBlockProperties)
                                    .build(),
                BracketHandlerMirror.<IIngredient>builder(classTree)
                                    .setRegex("\\*")
                                    .setType(IIngredient.class)
                                    .setEntries(Collections.singleton(IngredientAny.INSTANCE))
                                    .setIdMapper(it -> "*")
                                    .build(),
                BracketHandlerMirror.<String>builder(classTree)
                                    .setType(ILiquidStack.class)
                                    .setRegex("(fluid|liquid):.*")
                                    .setEntries(FluidRegistry.getRegisteredFluids().keySet())
                                    .setIdMapper(it -> "liquid:" + it.replace(" ", ""))
                                    .setPropertiesAdder((liquid, properties) -> {
                                        FluidStack fluidStack = Objects.requireNonNull(FluidRegistry.getFluidStack(liquid, Fluid.BUCKET_VOLUME));
                                        properties.add("name", fluidStack.getLocalizedName(), true);
                                    })
                                    .build(),
                BracketHandlerMirror.<IBiome>builder(classTree)
                                    .setType(IBiome.class)
                                    .setRegex("biome:.*")
                                    .setEntries(CraftTweakerAPI.game.getBiomes())
                                    .setIdMapper(it -> "biome:" + it.getId().split(":")[1])
                                    .build(),
                BracketHandlerMirror.<String>builder(classTree)
                                    .setType(ICreativeTab.class)
                                    .setRegex("creativetab:.*")
                                    .setEntries(CraftTweakerMC.creativeTabs.keySet())
                                    .setIdMapper("creativetab:"::concat)
                                    .build(),
                BracketHandlerMirror.<Method>builder(classTree)
                                    .setType(IDamageSource.class)
                                    .setRegex("damageSource:.*")
                                    .setEntries(Arrays.stream(MCDamageSourceExpand.class.getDeclaredMethods())
                                                      .filter(it -> it.getParameterCount() == 0)
                                                      .collect(Collectors.toList())
                                    )
                                    .setIdMapper(it -> "damageSource:" + it.getName())
                                    .build(),
                BracketHandlerMirror.<String>builder(classTree)
                                    .setType(IEnchantmentDefinition.class)
                                    .setRegex("enchantment:.*")
                                    .setEntries(BracketHandlerEnchantments.enchantments.keySet())
                                    .setIdMapper(it -> "enchantment:" + it)
                                    .build(),
                BracketHandlerMirror.<IEntityDefinition>builder(classTree)
                                    .setType(IEntityDefinition.class)
                                    .setRegex("entity:.*")
                                    .setEntries(CraftTweakerAPI.game.getEntities())
                                    .setIdMapper(it -> "entity:" + it.getId())
                                    .build(),
                BracketHandlerMirror.<IOreDictEntry>builder(classTree)
                                    .setType(IOreDictEntry.class)
                                    .setRegex("ore:.*")
                                    .setEntries(CraftTweakerAPI.oreDict.getEntries())
                                    .setIdMapper(it -> "ore:" + it.getName())
                                    .setPropertiesAdder((od, properties) -> {
                                        properties.add("name", ProbeZS.safeGetItemName(od.getFirstItem()), true);
                                    })
                                    .build(),
                BracketHandlerMirror.<Potion>builder(classTree)
                                    .setType(IPotion.class)
                                    .setRegex("potion:.*")
                                    .setEntries(ForgeRegistries.POTIONS.getValuesCollection())
                                    .setIdMapper(it -> "potion:" + it.getRegistryName())
                                    .build(),
                BracketHandlerMirror.<PotionType>builder(classTree)
                                    .setType(IPotionType.class)
                                    .setRegex("potiontype:.*")
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

    private void dumpEnvironment(Path dzsPath) {
        Environment.put("probezsSocketPort", String.valueOf(ProbeZSConfig.socketPort));
        Environment.put("probezsVersion", ProbeZS.VERSION);
        CrashReportCategory category = new CrashReport("", new Throwable()).getCategory();
        Map<String, CrashReportCategory.Entry> entries = category.children.stream()
                                                                          .collect(Collectors.toMap(CrashReportCategory.Entry::getKey, Function.identity()));
        Environment.put("operatingSystem", entries.get("Operating System").getValue());
        Environment.put("javaVersion", entries.get("Java Version").getValue());
        Environment.put("javaPath", System.getProperty("java.home"));
        Environment.put("jvmVersion", entries.get("Java VM Version").getValue());
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
        Environment.put("jvmFlags", runtimemxbean.getInputArguments());
        Environment.put("classpath", runtimemxbean.getClassPath());
        try {
            Environment.put("bootClassPath", runtimemxbean.getBootClassPath());
        } catch (UnsupportedOperationException e) {
            Environment.put("bootClassPath", "");
        }
        Environment.output(dzsPath.resolve("env.json"));
    }

    private static class ContentTweaker {

        @Optional.Method(modid = "contenttweaker")
        private static List<BracketHandlerMirror> dumpCoTBracketMirrors(ZenClassTree tree) {
            return Lists.newArrayList(
                    BracketHandlerMirror.<String>builder(tree)
                                        .setType(IBlockMaterialDefinition.class)
                                        .setRegex("blockmaterial:.*")
                                        .setEntries(ContentTweakerAPI.getInstance().getBlockMaterials().getAllNames())
                                        .setIdMapper("blockmaterial:"::concat)
                                        .build(),
                    BracketHandlerMirror.<String>builder(tree)
                                        .setRegex("soundtype:.*")
                                        .setType(ISoundTypeDefinition.class)
                                        .setEntries(ContentTweakerAPI.getInstance().getSoundTypes().getAllNames())
                                        .setIdMapper("soundtype:"::concat)
                                        .build(),
                    BracketHandlerMirror.<String>builder(tree)
                                        .setRegex("soundevent:.*")
                                        .setType(ISoundEventDefinition.class)
                                        .setEntries(ContentTweakerAPI.getInstance().getSoundEvents().getAllNames())
                                        .setIdMapper("soundevent:"::concat)
                                        .build(),
                    BracketHandlerMirror.<MaterialPart>builder(tree)
                                        .setRegex("materialpart:.*")
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
