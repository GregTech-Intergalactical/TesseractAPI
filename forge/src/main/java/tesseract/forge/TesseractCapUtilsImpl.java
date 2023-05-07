package tesseract.forge;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.forge.fluid.ForgeFluidContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.TesseractCapUtils;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.forge.wrapper.EnergyTileWrapper;
import tesseract.api.forge.wrapper.FluidTileWrapper;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class TesseractCapUtilsImpl {
    public static Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        return stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(e -> e);
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

    public static LazyOptional<IItemHandler> getLazyItemHandler(BlockEntity entity, Direction side){
        return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IFluidHandler> capability = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capSide);
        if (capability.isPresent()) {
            if (capCallback != null) capability.addListener(o -> capCallback.run());
            IFluidHandler handler = capability.map(f -> f).orElse(null);
            if (handler instanceof ForgeFluidContainer container){
                FluidContainer container1 = container.container().getContainer(capSide);
                if (container1 instanceof IFluidNode node) return node;
            }
            return handler instanceof IFluidNode ? (IFluidNode) handler: new FluidTileWrapper(tile, handler);
        } else {
            return null;
        }
    }
}
