package tesseract.fabric;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyMoveable;
import earth.terrarium.botarium.common.energy.base.PlatformItemEnergyManager;
import earth.terrarium.botarium.common.energy.util.EnergyHooks;
import net.fabricatedforgeapi.transfer.fluid.FluidStorageHandler;
import net.fabricatedforgeapi.transfer.fluid.FluidStorageHandlerItem;
import net.fabricatedforgeapi.transfer.item.ItemStorageHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl;
import tesseract.TesseractCapUtils;
import tesseract.TesseractConfig;
import tesseract.api.fabric.TesseractLookups;
import tesseract.api.fabric.TileListeners;
import tesseract.api.fabric.wrapper.*;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.item.IItemNode;
import tesseract.api.wrapper.FluidTileWrapper;
import tesseract.api.wrapper.ItemTileWrapper;

import java.util.Optional;

public class TesseractCapUtilsImpl {
    public static Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        IEnergyHandlerItem energyHandler = ContainerItemContext.withInitial(stack).find(TesseractLookups.ENERGY_HANDLER_ITEM);
        return Optional.ofNullable(energyHandler);
    }

    public static Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack){
        IEnergyHandlerItem energyHandler = ContainerItemContext.withInitial(stack).find(TesseractLookups.ENERGY_HANDLER_ITEM);
        if (energyHandler == null){
            EnergyStorage storage = ContainerItemContext.withInitial(stack).find(EnergyStorage.ITEM);
            if (storage instanceof IEnergyHandlerItem e){
                energyHandler = e;
            } else if (EnergyHooks.isEnergyItem(stack)){
                PlatformItemEnergyManager itemEnergyManager = EnergyHooks.getItemEnergyManager(stack);
                energyHandler = new EnergyStackWrapper(stack, itemEnergyManager);
            }
        }
        return Optional.ofNullable(energyHandler);
    }

    public static Optional<IEnergyHandler> getEnergyHandler(@NotNull BlockEntity entity, Direction side){
        IEnergyHandler energyHandler = TesseractLookups.ENERGY_HANDLER_SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        if (energyHandler == null) {
            if (FabricLoader.getInstance().isModLoaded("modern_industrialization") && TesseractConfig.COMMON.ENABLE_MI_COMPAT){
                energyHandler = getEnergyMoveable(entity, side);
                if (energyHandler != null) return Optional.of(energyHandler);
            }
            energyHandler = getEnergyStorage(entity, side);
        }
        return Optional.ofNullable(energyHandler);
    }

    public static Optional<IHeatHandler> getHeatHandler(BlockEntity entity, Direction side){
        IHeatHandler heatHandler = TesseractLookups.HEAT_HANDLER_SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        return Optional.ofNullable(heatHandler);
    }

    //TODO figure out better abstraction method
    public static Optional<IItemHandler> getItemHandler(BlockEntity entity, Direction side){
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        if (storage instanceof IItemHandler itemHandler) return Optional.of(itemHandler);
        return storage == null ? Optional.empty() : Optional.of(new ItemStorageHandler(storage));
    }

    public static Optional<IFluidHandler> getFluidHandler(BlockEntity entity, Direction side){
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        if (storage instanceof IFluidHandler fluidHandler) return Optional.of(fluidHandler);
        return storage == null ? Optional.empty() : Optional.of(new FluidStorageHandler(storage));
    }

    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        Optional<IFluidHandler> capability = getFluidHandler(tile, capSide);
        if (capability.isPresent()) {
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            IFluidHandler handler = capability.get();
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
        Optional<IItemHandler> h = getItemHandler(tile, capSide);
        if (h.isPresent()) {
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            if (h.map(t -> t instanceof IItemNode).orElse(false)) {
                return (IItemNode) h.get();
            }
            return new ItemTileWrapper(tile, h.get());
        }
        return null;
    }

    private static IEnergyHandler getEnergyStorage(BlockEntity be, Direction direction){
        EnergyStorage storage = EnergyStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, direction);
        if (storage == null) return null;
        if (storage instanceof IEnergyHandler moveable1) return moveable1;
        if (storage instanceof IEnergyHandlerStorage handlerStorage) return handlerStorage.getEnergyHandler();
        return new EnergyTileWrapper(be, storage);
    }

    private static IEnergyHandler getEnergyMoveable(BlockEntity be, Direction direction){
        EnergyMoveable moveable = EnergyApi.MOVEABLE.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, direction);
        if (moveable == null) return null;
        if (moveable instanceof IEnergyHandler moveable1) return moveable1;
        if (moveable instanceof IEnergyHandlerMoveable moveable1) return moveable1.getEnergyHandler();
        return new EnergyMoveableWrapper(be, moveable);
    }

    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        if(tile instanceof FluidAttachment attachment && attachment.getFluidHolderType() == BlockEntity.class) {
            FluidContainer container = attachment.getFluidContainer(tile).getContainer(capSide);
            if (container instanceof IFluidNode node) return node;
            else return new FluidContainerWrapper(container);
        }
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, capSide);
        if (storage != null){
            return storage instanceof IFluidNode node ? node : new FluidTileWrapper(tile, storage);
        }
        return null;
    }
}
