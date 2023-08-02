package youyihj.probezs.bracket;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import youyihj.probezs.tree.ZenClassTree;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author youyihj
 */
public class BlockBracketNode extends ZenBracketNode {
    public BlockBracketNode(ZenClassTree tree) {
        super(tree.createLazyClassNode(crafttweaker.api.block.IBlockState.class));
        readBlocks();
    }

    @Override
    public void fillJsonContents(JsonArray jsonArray) {
        fillJsonContents0(jsonArray);
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> void fillJsonContents0(JsonArray jsonArray) {
        for (Block block : ForgeRegistries.BLOCKS) {
            JsonObject element = new JsonObject();
            element.addProperty("id", "blockstate:" + block.getRegistryName());
            JsonObject properties = new JsonObject();
            for (IProperty<?> property : block.getBlockState().getProperties()) {
                IProperty<T> propertyT = ((IProperty<T>) property);
                JsonArray values = new JsonArray();
                for (T allowedValue : propertyT.getAllowedValues()) {
                    values.add(propertyT.getName(allowedValue));
                }
                properties.add(property.getName(), values);
            }
            element.add("properties", properties);
            jsonArray.add(element);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> void readBlocks() {
        for (Block block : ForgeRegistries.BLOCKS) {
            StringBuilder directId = new StringBuilder("blockstate:");
            directId.append(block.getRegistryName());
            addContent(directId.toString());
            ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();
            if (validStates.size() > 1) {
                directId.append(":");
                for (IBlockState validState : validStates) {
                    ImmutableMap<IProperty<?>, Comparable<?>> properties = validState.getProperties();
                    StringJoiner joiner = new StringJoiner(",", directId.toString(), "");
                    for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                        IProperty<T> property = ((IProperty<T>) entry.getKey());
                        T value = (T) entry.getValue();
                        joiner.add(property.getName() + "=" + property.getName(value));
                    }
                    addContent(joiner.toString());
                }
            }
        }
    }
}
