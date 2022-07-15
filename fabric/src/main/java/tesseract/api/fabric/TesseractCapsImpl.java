package tesseract.api.fabric;


import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.DelegatingEnergyStorage;
import team.reborn.energy.api.base.SimpleBatteryItem;
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;

public class TesseractCapsImpl {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(IEnergyHandler.class);

    public static final Capability<IHeatHandler> HEAT_CAPABILITY = CapabilityManager.get(IHeatHandler.class);

    public static final BlockApiLookup<IEnergyHandler, Direction> ENERGY_HANDLER_SIDED =
            BlockApiLookup.get(new ResourceLocation("tesseractapi:sided_gt_energy"), IEnergyHandler.class, Direction.class);

    public static final ItemApiLookup<IEnergyHandler, ContainerItemContext> ENERGY_HANDLER_ITEM =
            ItemApiLookup.get(new ResourceLocation("tesseractapi:gt_energy"), IEnergyHandler.class, ContainerItemContext.class);

    public static final BlockApiLookup<IHeatHandler, Direction> HEAT_HANDLER_SIDED =
            BlockApiLookup.get(new ResourceLocation("tesseractapi:sided_heat"), IHeatHandler.class, Direction.class);

    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return ENERGY_HANDLER_CAPABILITY;
    }
    public static Capability<IHeatHandler> getHEAT_CAPABILITY(){
        return HEAT_CAPABILITY;
    }
}
