package tesseract.forge;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import earth.terrarium.botarium.forge.fluid.ForgeFluidContainer;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.forge.wrapper.CauldronWrapper;
import tesseract.api.forge.wrapper.EnergyStackWrapper;
import tesseract.api.forge.wrapper.EnergyTileWrapper;
import tesseract.api.forge.wrapper.FluidTileWrapper;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.item.IItemNode;
import tesseract.api.wrapper.ItemTileWrapper;

import javax.annotation.Nullable;
import java.util.Optional;

public class TesseractCapUtilsImpl {
    public static Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        return stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(e -> e);
    }

    public static Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack){
        IEnergyHandlerItem energyHandler = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(e -> e).orElse(null);
        if (energyHandler == null){
            IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY).map(e -> e).orElse(null);
            if (storage instanceof IEnergyHandlerItem e){
                energyHandler = e;
            } else if (storage != null){
                energyHandler = new EnergyStackWrapper(stack, storage);
            }
        }
        return Optional.ofNullable(energyHandler);
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
        return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).resolve();
    }
    public static Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side){
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null){
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractCauldronBlock){
                return Optional.of(new ForgeFluidHandler(new CauldronWrapper(state, level, pos)));
            }
            return Optional.empty();
        }
        return FluidHooks.safeGetBlockFluidManager(entity, side);
    }

    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IFluidHandler> capability;
        if (tile == null){
            BlockState state = level.getBlockState(BlockPos.of(pos));
            if (state.getBlock() instanceof AbstractCauldronBlock cauldronBlock){
                capability = LazyOptional.of(() -> new CauldronWrapper(state, level, BlockPos.of(pos)));
            } else {
                return null;
            }
        } else {
            capability = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capSide);
        }
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

    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IItemHandler> h = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, capSide);
        if (h.isPresent()) {
            if (capCallback != null) h.addListener(t -> capCallback.run());
            if (h.map(t -> t instanceof IItemNode).orElse(false)) {
                return (IItemNode) h.resolve().get();
            }
            return new ItemTileWrapper(tile, h.orElse(null));
        }
        return null;
    }
}
