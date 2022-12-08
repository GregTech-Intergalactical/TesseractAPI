package tesseract.forge;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.forge.wrapper.EnergyTileWrapper;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.heat.IHeatHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class TesseractCapUtilsImpl {
    public static Optional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        return stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY).map(e -> e);
    }

    public static Optional<IEnergyHandler> getEnergyHandler(BlockEntity entity, Direction side){
        LazyOptional<IEnergyHandler> energyHandler = entity.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY, side);
        if (energyHandler.isPresent()) return energyHandler.map(e -> e);
        energyHandler = getWrappedHandler(entity, side);
        return energyHandler.map(e -> e);
    }

    public static LazyOptional<IEnergyHandler> getWrappedHandler(BlockEntity be, @Nullable Direction side){
        IEnergyStorage storage = be.getCapability(CapabilityEnergy.ENERGY, side).map(i -> i).orElse(null);
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof IEnergyHandlerStorage handlerStorage) return LazyOptional.of(handlerStorage::getEnergyHandler);
        return LazyOptional.of(() -> new EnergyTileWrapper(be, storage));
    }

    public static Optional<IHeatHandler> getHeatHandler(BlockEntity entity, Direction side){
        return entity.getCapability(TesseractCaps.HEAT_CAPABILITY, side).map(e -> e);
    }

    public static Optional<IItemHandler> getItemHandler(BlockEntity entity, Direction side){
        return getLazyItemHandler(entity, side).map(i -> i);
    }

    public static Optional<IFluidHandler> getFluidHandler(BlockEntity entity, Direction side){
        return getLazyFluidHandler(entity, side).map(f -> f);
    }

    public static LazyOptional<IItemHandler> getLazyItemHandler(BlockEntity entity, Direction side){
        return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    public static LazyOptional<IFluidHandler> getLazyFluidHandler(BlockEntity entity, Direction side){
        return entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }
}
