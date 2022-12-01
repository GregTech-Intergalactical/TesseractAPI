package tesseract.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.forge.TesseractCapsImpl;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.wrapper.FluidTileWrapper;
import tesseract.api.forge.wrapper.EnergyTileWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.wrapper.ItemTileWrapper;

import javax.annotation.Nullable;

public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = tile.getCapability(TesseractCapsImpl.ENERGY_HANDLER_CAPABILITY, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.orElse(null);
        } else {
            LazyOptional<IEnergyHandler> cap = getWrappedHandler(tile, direction);
            if (cap.isPresent()) {
                if (invalidate != null )cap.addListener(o -> invalidate.run());
                return capability.orElse(null);
            }
        }
        return null;
    }

    public static boolean isFeCap(Capability<?> cap){
        return cap == CapabilityEnergy.ENERGY;
    }

    public static boolean tileHasFEOrTRE(BlockEntity entity, Direction side){
        return entity.getCapability(CapabilityEnergy.ENERGY, side).isPresent();
    }

    public static LazyOptional<IEnergyHandler> getWrappedHandler(BlockEntity be, @Nullable Direction side){
        IEnergyStorage storage = be.getCapability(CapabilityEnergy.ENERGY, side).map(i -> i).orElse(null);
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof IEnergyHandlerStorage handlerStorage) return LazyOptional.of(handlerStorage::getEnergyHandler);
        return LazyOptional.of(() -> new EnergyTileWrapper(be, storage));
    }

    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        if (stack == null || stack.isEmpty()) return LazyOptional.empty();
        return stack.getCapability(TesseractCapsImpl.ENERGY_HANDLER_CAPABILITY);
    }

    public static boolean isForge(){
        return true;
    }
}
