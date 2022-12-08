package tesseract.api.fabric;


import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.heat.IHeatHandler;

public class TesseractLookups {

    public static final BlockApiLookup<IEnergyHandler, Direction> ENERGY_HANDLER_SIDED =
            BlockApiLookup.get(new ResourceLocation("tesseractapi:sided_gt_energy"), IEnergyHandler.class, Direction.class);

    public static final ItemApiLookup<IEnergyHandler, ContainerItemContext> ENERGY_HANDLER_ITEM =
            ItemApiLookup.get(new ResourceLocation("tesseractapi:gt_energy"), IEnergyHandler.class, ContainerItemContext.class);

    public static final BlockApiLookup<IHeatHandler, Direction> HEAT_HANDLER_SIDED =
            BlockApiLookup.get(new ResourceLocation("tesseractapi:sided_heat"), IHeatHandler.class, Direction.class);
}
