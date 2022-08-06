package tesseract.fabric;


import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyMoveable;
import net.fabricatedforgeapi.transfer.fluid.FluidStorageHandler;
import net.fabricatedforgeapi.transfer.fluid.FluidStorageHandlerItem;
import net.fabricatedforgeapi.transfer.fluid.IFluidHandlerStorage;
import net.fabricatedforgeapi.transfer.item.IItemHandlerStorage;
import net.fabricatedforgeapi.transfer.item.ItemStorageHandler;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import team.reborn.energy.api.EnergyStorage;
import tesseract.TesseractConfig;
import tesseract.api.TesseractCaps;
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
        LazyOptional<IEnergyHandler> capability = tile.getCapability(TesseractCapsImpl.ENERGY_HANDLER_CAPABILITY, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        } else {
            capability = getWrappedHandler(tile, direction);
            if (capability.isPresent()) {
                if (invalidate != null) capability.addListener(t -> invalidate.run());
                return capability.resolve().get();
            }
        }
        return null;
    }

    public static boolean isFeCap(Capability<?> cap){
        return false;
    }

    public static LazyOptional<IEnergyHandler> getWrappedHandler(BlockEntity be, @Nullable Direction side){
        Level l = be.getLevel();
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();
        IEnergyHandler energyHandler = TesseractCapsImpl.ENERGY_HANDLER_SIDED.find(l, pos, state, be, side);
        if (energyHandler != null) return LazyOptional.of(() -> energyHandler);
        if (FabricLoader.getInstance().isModLoaded("modern_industrialization") && TesseractConfig.COMMON.ENABLE_MI_COMPAT){
            IEnergyHandler handler = getEnergyMoveable(l, pos, state, be, side);
            if (handler != null) return LazyOptional.of(() -> handler);
        }
        IEnergyHandler storage = getEnergyStorage(l, pos, state, be, side);
        return storage == null ? LazyOptional.empty() : LazyOptional.of(() -> storage);
    }

    private static IEnergyHandler getEnergyStorage(Level level, BlockPos pos, BlockState state, BlockEntity be, Direction side){
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, state, be, side);
        if (storage == null) return null;
        if (storage instanceof IEnergyHandlerStorage handlerStorage) return handlerStorage.getEnergyHandler();
        return new EnergyTileWrapper(be, storage);
    }

    private static IEnergyHandler getEnergyMoveable(Level level, BlockPos pos, BlockState state, BlockEntity be, Direction side){
        EnergyMoveable moveable = EnergyApi.MOVEABLE.find(level, pos, state, be, side);
        if (moveable == null) return null;
        if (moveable instanceof IEnergyHandlerMoveable moveable1) return moveable1.getEnergyHandler();
        return new EnergyMoveableWrapper(be, moveable);
    }

    //TODO
    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        return stack.getCapability(TesseractCaps.getENERGY_HANDLER_CAPABILITY());
    }

    public static boolean isForge(){
        return false;
    }
}
