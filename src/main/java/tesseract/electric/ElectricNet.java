package tesseract.electric;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.Graph;

public class ElectricNet {
    public static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRICITY = new Int2ObjectOpenHashMap<>();

    public ElectricNet() {
        for (DimensionType dim : DimensionType.getAll()) {
            ELECTRICITY.put(dim.getId(), new Graph<>());
        }
    }

    public static Graph<IElectricCable, IElectricNode> instance(World world) {
        return ELECTRICITY.get(world.getDimension().getType().getId());
    }
}
