package tesseract.fabric;

import net.fabricatedforgeapi.caps.CapUtils;
import net.fabricatedforgeapi.fluid.FluidHandlerStorage;
import net.fabricatedforgeapi.fluid.FluidStorageHandler;
import net.fabricatedforgeapi.fluid.FluidStorageHandlerItem;
import net.fabricatedforgeapi.fluid.IFluidHandlerStorage;
import net.fabricatedforgeapi.item.IItemHandlerStorage;
import net.fabricatedforgeapi.item.ItemHandlerStorage;
import net.fabricatedforgeapi.item.ItemStorageHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import team.reborn.energy.api.EnergyStorage;
import tesseract.api.fabric.TesseractCapsImpl;
import tesseract.api.fabric.wrapper.*;
import tesseract.api.fluid.IFluidTransferable;
import tesseract.api.wrapper.ItemTileWrapper;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemTransferable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractCapsImpl.getGTEnergyHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        } else if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")){
            EnergyStorage storage = EnergyStorage.SIDED.find(level, BlockPos.of(pos), level.getBlockState(BlockPos.of(pos)), tile, direction);
            if (storage != null) {
                return new EnergyTileWrapper(tile, storage);
            }
        }
        return null;
    }

    public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, @Nullable Direction side) {
        // lib handling
        if (be instanceof ICapabilityProvider p) return p.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        if (be instanceof IItemTransferable transferable) return transferable.getItemHandler(side);
        return CapUtils.getWrappedItemHandler(be, side);
    }

    public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, @Nullable Direction side) {
        boolean client = Objects.requireNonNull(be.getLevel()).isClientSide();
        // lib handling
        if (be instanceof ICapabilityProvider prov) return prov.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        if (be instanceof IFluidTransferable transferable) {
            /*if (client && !transferable.shouldRunClientSide()) {
                return LazyOptional.empty();
            }*/
            return transferable.getFluidHandler(side);
        }
        return CapUtils.getWrappedFluidHandler(be, side);
    }

    // Fluid-containing items

    public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return LazyOptional.empty();
        ContainerItemContext ctx = ContainerItemContext.withInitial(stack);
        Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ctx);
        return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(ctx, fluidStorage));
    }

    //TODO
    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        return LazyOptional.empty();
    }

    private static Direction[] getDirections(@Nullable Direction direction) {
        if (direction == null) return Direction.values();
        return new Direction[] {direction};
    }

    public static LazyOptional<IItemHandler> simplifyItem(Storage<ItemVariant> storage) {
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof IItemHandlerStorage handler) return LazyOptional.ofObject(handler.getHandler());
        return LazyOptional.ofObject(new ItemStorageHandler(storage));
    }

    public static LazyOptional<IFluidHandler> simplifyFluid(Storage<FluidVariant> storage) {
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof IFluidHandlerStorage handler) return LazyOptional.ofObject(handler.getHandler());
        return LazyOptional.ofObject(new FluidStorageHandler(storage));
    }

    public static boolean isForge(){
        return false;
    }
}
