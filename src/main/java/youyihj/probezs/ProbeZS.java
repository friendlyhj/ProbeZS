package youyihj.probezs;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
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
import crafttweaker.mc1120.damage.expand.MCDamageSourceExpand;
import crafttweaker.mc1120.util.CraftTweakerHacks;
import crafttweaker.preprocessor.PreprocessorFactory;
import crafttweaker.zenscript.GlobalRegistry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import youyihj.probezs.api.BracketHandlerServiceImpl;
import youyihj.probezs.bracket.BracketHandlerEntryProperties;
import youyihj.probezs.bracket.BracketHandlerMirror;
import youyihj.probezs.core.ASMMemberCollector;
import youyihj.probezs.member.MemberFactory;
import youyihj.probezs.member.reflection.ReflectionMemberFactory;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.global.ZenGlobalMemberTree;
import youyihj.probezs.util.FileUtils;
import youyihj.probezs.util.LoadingObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.18.5";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";
    public static Logger logger;
    private static final List<LoadingObject<?>> loadingObjects = new ArrayList<>();

    public volatile LoadingObject<String> mappings = LoadingObject.of("");

    public Path generatedPath = processGeneratedPath();
    public static final Map<String, ModContainer> pathToModMap = new HashMap<>();


    private static final Supplier<MemberFactory> MEMBER_FACTORY = Suppliers.memoize(() -> {
        switch (ProbeZSConfig.memberCollector) {
            case REFLECTION:
                return new ReflectionMemberFactory();
            case ASM:
                return ASMMemberCollector.MEMBER_FACTORY;
            default:
                throw new AssertionError();
        }
    });

    @Mod.Instance
    public static ProbeZS instance;

    public static void addLoadingObject(LoadingObject<?> object) {
        loadingObjects.add(object);
    }

    public static MemberFactory getMemberFactory() {
        return MEMBER_FACTORY.get();
    }

    private static Path processGeneratedPath() {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            Path scriptsPath;
            try {
                scriptsPath = FileSystems.getDefault().getPath("scripts").toRealPath();
            } catch (IOException e) {
                scriptsPath = FileSystems.getDefault().getPath(System.getProperty("user.dir")).resolve("scripts");
            }
            Environment.put("scriptPath", scriptsPath.toString());
            sha1.update(scriptsPath.toString().getBytes(StandardCharsets.UTF_8));
            String userHome = System.getProperty("user.home");
            return FileSystems.getDefault().getPath(userHome)
                              .resolve(".probezs")
                              .resolve(Hex.encodeHexString(sha1.digest()))
                              .resolve("generated");
        } catch (NoSuchAlgorithmException e) { // really?
            throw new AssertionError();
        }
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        new Thread(() -> {
            try {
                URL url = new URL("https://friendlyhj.github.io/probezs-mappings/method-parameter-names.yaml");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    mappings.set(reader.lines().collect(Collectors.joining("\n")));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        dumpEnvironment();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        try {
            registerRMI();
        } catch (RemoteException e) {
            logger.error(e);
        }
//        try {
//            Files.walkFileTree(Paths.get("scripts"), new SimpleFileVisitor<Path>() {
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    if (file.toString().endsWith(".dzs") || file.toString().endsWith(".json")) {
//                        Files.delete(file);
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            ProbeZS.logger.error("Failed to delete previous dzs", e);
//        }
        ZenClassTree root = ZenClassTree.getRoot();
        ZenGlobalMemberTree globalMemberTree = dumpGlobalMembers(root);
        List<BracketHandlerMirror> mirrors = dumpBracketHandlerMirrors(root);
        root.fresh();
        root.output();
        globalMemberTree.output();
        outputBracketHandlerMirrors(mirrors);
        outputPreprocessors();
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        loadingObjects.forEach(LoadingObject::setAlreadyLoaded);
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
                                    .setEntries(safeGetItemRegistry())
                                    .setRegex(".*")
                                    .setIdMapper(it -> {
                                        String commandString = it.toCommandString();
                                        return commandString.substring(1, commandString.length() - 1);
                                    })
                                    .setPropertiesAdder((item, properties) -> {
                                        properties.add("name", safeGetItemName(item), true);
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

    private void outputPreprocessors() {
        HashMap<String, PreprocessorFactory<?>> registeredPreprocessorActions = CraftTweakerHacks.getPrivateObject(CraftTweakerAPI.tweaker.getPreprocessorManager(), "registeredPreprocessorActions");
        try {
            FileUtils.createFile(
                    generatedPath.resolve("preprocessors.json"),
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

    private void outputBracketHandlerMirrors(List<BracketHandlerMirror> mirrors) {
        String json = BracketHandlerMirror.GSON.toJson(mirrors);
        try {
            FileUtils.createFile(generatedPath.resolve("brackets.json"), json);
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

    private void dumpEnvironment() {
        Environment.put("probezsSocketPort", String.valueOf(ProbeZSConfig.socketPort));
        Environment.put("probezsVersion", VERSION);
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
        Environment.output(generatedPath.resolve("env.json"));
    }

    public static String safeGetItemName(IItemStack item) {
        try {
            if (item.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                item = item.withDamage(0);
            }
            return TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName());
        } catch (Exception e) {
            item = item.withDamage(0);
            try {
                return TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName());
            } catch (Exception ex) {
                return "ERROR";
            }
        }
    }

    public static List<IItemStack> safeGetItemRegistry() {
        List<IItemStack> items = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item == Items.AIR)
                continue;
            NonNullList<ItemStack> subItems = NonNullList.create();
            CreativeTabs tab = item.getCreativeTab() != null ? item.getCreativeTab() : CreativeTabs.SEARCH;
            item.getSubItems(tab, subItems);
            IntSet validMetas = new IntArraySet();
            for (ItemStack subItem : subItems) {
                validMetas.add(subItem.getMetadata());
            }
            for (int validMeta : validMetas.toIntArray()) {
                IItemStack ctItem = CraftTweakerMC.getIItemStackMutable(new ItemStack(item, 1, validMeta));
                items.add(ctItem);
            }
        }
        return items;
    }

    public String getClassOwner(Class<?> clazz) {
        return java.util.Optional.of(clazz)
                                 .map(Class::getProtectionDomain)
                                 .map(ProtectionDomain::getCodeSource)
                                 .map(CodeSource::getLocation)
                                 .map(URL::getFile)
                                 .map(it -> it.split("!")[0])
                                 .map(pathToModMap::get)
                                 .map(ModContainer::getModId)
                                 .orElse(null);
    }

    private void registerRMI() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(ProbeZSConfig.socketPort);
        Remote remote = new BracketHandlerServiceImpl();
        UnicastRemoteObject.exportObject(remote, ProbeZSConfig.socketPort);
        registry.rebind("BracketHandler", remote);
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
