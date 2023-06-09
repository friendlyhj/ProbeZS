package youyihj.probezs;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.creativetabs.ICreativeTab;
import crafttweaker.api.damage.IDamageSource;
import crafttweaker.api.enchantments.IEnchantmentDefinition;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.api.potions.IPotion;
import crafttweaker.api.potions.IPotionType;
import crafttweaker.api.world.IBiome;
import crafttweaker.mc1120.brackets.BracketHandlerEnchantments;
import crafttweaker.mc1120.damage.expand.MCDamageSourceExpand;
import crafttweaker.zenscript.GlobalRegistry;
import crafttweaker.zenscript.IBracketHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import stanhebben.zenscript.util.Pair;
import youyihj.probezs.bracket.BlockBracketNode;
import youyihj.probezs.bracket.ItemBracketNode;
import youyihj.probezs.bracket.ZenBracketTree;
import youyihj.probezs.tree.ZenClassTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.6.0";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";

    public static String mappings = "";

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        new Thread(() -> {
            try {
                URL url = new URL("https://friendlyhj.github.io/probezs-mappings/method-parameter-names.yaml");
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    mappings = reader.lines().collect(Collectors.joining("\n"));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ZenClassTree root = ZenClassTree.getRoot();
        root.readGlobals(GlobalRegistry.getGlobals());
        ZenBracketTree bracketTree = dumpBracketHandlers(root);
        root.fresh();
        root.output();
        bracketTree.output();
    }

    private ZenBracketTree dumpBracketHandlers(ZenClassTree classTree) {
        ZenBracketTree bracketTree = new ZenBracketTree(classTree);
        bracketTree.addNode(IItemStack.class, new ItemBracketNode(classTree));
        bracketTree.addNode(IBlockState.class, new BlockBracketNode(classTree));
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
}
