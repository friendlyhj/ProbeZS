package youyihj.probezs;

import com.google.common.base.Suppliers;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.commands.CTChatCommand;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;
import youyihj.probezs.api.BracketHandlerServiceImpl;
import youyihj.probezs.core.ASMMemberCollector;
import youyihj.probezs.member.MemberFactory;
import youyihj.probezs.member.reflection.ReflectionMemberFactory;
import youyihj.probezs.util.LoadingObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
@Mod(modid = ProbeZS.MODID, name = ProbeZS.NAME, version = ProbeZS.VERSION, dependencies = ProbeZS.DEPENDENCIES)
public class ProbeZS {
    public static final String MODID = "probezs";
    public static final String VERSION = "1.19.0";
    public static final String NAME = "ProbeZS";
    public static final String DEPENDENCIES = "required-after:crafttweaker;";
    public static Logger logger;
    private static final List<LoadingObject<?>> loadingObjects = new ArrayList<>();

    public CompletableFuture<String> mappingsFuture = CompletableFuture.supplyAsync(() -> {
        try {
            URL url = new URL("https://friendlyhj.github.io/probezs-mappings/method-parameter-names.yaml");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    });

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

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        try {
            registerRMI();
        } catch (RemoteException e) {
            logger.error(e);
        }
        CTChatCommand.registerCommand(new ProbeZSCommand());
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        loadingObjects.forEach(LoadingObject::setAlreadyLoaded);
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


}
