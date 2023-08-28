package youyihj.probezs;

import com.google.common.base.Suppliers;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.creativetabs.ICreativeTab;
import crafttweaker.api.damage.IDamageSource;
import crafttweaker.api.enchantments.IEnchantmentDefinition;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
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
import crafttweaker.zenscript.IBracketHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import stanhebben.zenscript.util.Pair;
import youyihj.probezs.bracket.BlockBracketNode;
import youyihj.probezs.bracket.IngredientAnyBracketNode;
import youyihj.probezs.bracket.ItemBracketNode;
import youyihj.probezs.bracket.ZenBracketTree;
import youyihj.probezs.core.ASMMemberCollector;
import youyihj.probezs.member.MemberFactory;
import youyihj.probezs.member.reflection.ReflectionMemberFactory;
import youyihj.probezs.socket.SocketHandler;
import youyihj.probezs.tree.ZenClassTree;
import youyihj.probezs.tree.global.ZenGlobalMemberTree;
import youyihj.probezs.util.LoadingObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.10.3";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";
    public static Logger logger;
    private static final List<LoadingObject<?>> loadingObjects = new ArrayList<>();

    public LoadingObject<String> mappings = LoadingObject.of("");

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

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        new Thread(() -> {
            try {
                URL url = new URL("https://friendlyhj.github.io/probezs-mappings/method-parameter-names.yaml");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    mappings.set(reader.lines().collect(Collectors.joining("\n")));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        try {
            Files.walkFileTree(Paths.get("scripts"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".dzs") || file.toString().endsWith(".json")) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to delete previous dzs", e);
        }
        ZenClassTree root = ZenClassTree.getRoot();
        ZenBracketTree bracketTree = dumpBracketHandlers(root);
        ZenGlobalMemberTree globalMemberTree = dumpGlobalMembers(root);
        root.fresh();
        root.output();
        globalMemberTree.output();
        bracketTree.output();
        outputPreprocessors();
        if (ProbeZSConfig.socketProtocol != ProbeZSConfig.SocketProtocol.NONE) {
            SocketHandler.enable();
        }
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

    private ZenBracketTree dumpBracketHandlers(ZenClassTree classTree) {
        ZenBracketTree bracketTree = new ZenBracketTree(classTree);
        bracketTree.addNode(IItemStack.class, new ItemBracketNode(classTree));
        bracketTree.addNode(IBlockState.class, new BlockBracketNode(classTree));
        bracketTree.addNode(IIngredient.class, new IngredientAnyBracketNode(classTree));
        for (Pair<Integer, IBracketHandler> entry : GlobalRegistry.getPrioritizedBracketHandlers()) {
            bracketTree.addHandler(entry.getValue());
        }

        bracketTree.putContent("liquid", ILiquidStack.class,
                FluidRegistry.getRegisteredFluids().keySet().stream()
                        .map(it -> it.replace(" ", ""))
        );

        bracketTree.putContent("biome", IBiome.class,
                CraftTweakerAPI.game.getBiomes().stream()
                        .map(IBiome::getId)
                        .map(it -> it.split(":")[1])
        );

        bracketTree.putContent("creativetab", ICreativeTab.class, CraftTweakerMC.creativeTabs.keySet().stream());
        bracketTree.putContent("damageSource", IDamageSource.class, Arrays.stream(MCDamageSourceExpand.class.getDeclaredMethods())
                .filter(it -> it.getParameterCount() == 0)
                .map(Method::getName)
        );
        bracketTree.putContent("enchantment", IEnchantmentDefinition.class, BracketHandlerEnchantments.enchantments.keySet().stream());
        bracketTree.putContent("entity", IEntityDefinition.class, CraftTweakerAPI.game.getEntities().stream().map(IEntityDefinition::getId));
        bracketTree.putContent("ore", IOreDictEntry.class, CraftTweakerAPI.oreDict.getEntries().stream().map(IOreDictEntry::getName));
        bracketTree.putContent("potion", IPotion.class, ForgeRegistries.POTIONS.getKeys().stream().map(Objects::toString));
        bracketTree.putContent("potiontype", IPotionType.class, ForgeRegistries.POTION_TYPES.getKeys().stream().map(Objects::toString));
        return bracketTree;
    }

    private void outputPreprocessors() {
        HashMap<String, PreprocessorFactory<?>> registeredPreprocessorActions = CraftTweakerHacks.getPrivateObject(CraftTweakerAPI.tweaker.getPreprocessorManager(), "registeredPreprocessorActions");
        try {
            FileUtils.write(
                    new File("scripts/generated/preprocessors.json"),
                    registeredPreprocessorActions.keySet().stream().map(it -> StringUtils.wrap(it, "\"")).collect(Collectors.toList()).toString(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            ProbeZS.logger.error("Failed to output preprocessor json", e);
        }
    }
}
