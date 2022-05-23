package tesseract.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.forge.TesseractCapsImpl;
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
            return capability.resolve().get();
        } else {
            LazyOptional<IEnergyStorage> cap = tile.getCapability(CapabilityEnergy.ENERGY, direction);
            if (cap.isPresent()) {
                EnergyTileWrapper node = new EnergyTileWrapper(tile, cap.orElse(null));
                cap.addListener(o -> invalidate.run());
                return node;
            }
        }
        return null;
    }

    public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, @Nullable Direction side){
        return be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, @Nullable Direction side){
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return LazyOptional.empty();
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
    }

    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        if (stack == null || stack.isEmpty()) return LazyOptional.empty();
        return stack.getCapability(TesseractCapsImpl.ENERGY_HANDLER_CAPABILITY);
    }

    public static ResourceLocation getFluidId(Fluid fluid){
        return fluid.getRegistryName();
    }

    public static int getFluidTemperature(Fluid fluid){
        return fluid.getAttributes().getTemperature();
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return fluid.getAttributes().isGaseous();
    }

}
