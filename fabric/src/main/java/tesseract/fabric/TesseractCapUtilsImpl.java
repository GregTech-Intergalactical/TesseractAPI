package tesseract.fabric;

import earth.terrarium.botarium.api.energy.EnergyHooks;
import earth.terrarium.botarium.api.energy.PlatformItemEnergyManager;
import earth.terrarium.botarium.api.fluid.FluidContainer;
import earth.terrarium.botarium.api.fluid.FluidHoldingBlock;
import earth.terrarium.botarium.api.fluid.FluidHooks;
import earth.terrarium.botarium.api.fluid.PlatformFluidHandler;
import earth.terrarium.botarium.fabric.fluid.FabricFluidHandler;
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
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;
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
import tesseract.api.item.PlatformItemHandler;
import tesseract.mixin.fabric.FabricBlockFluidContainerAccessor;

import java.util.Optional;

public class TesseractCapUtilsImpl implements TesseractCapUtils {
    @Override
    public Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        IEnergyHandlerItem energyHandler = ContainerItemContext.withInitial(stack).find(TesseractLookups.ENERGY_HANDLER_ITEM);
        return Optional.ofNullable(energyHandler);
    }

    @Override
    public Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack){
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

    @Override
    public Optional<IEnergyHandler> getEnergyHandler(@NotNull BlockEntity entity, Direction side){
        IEnergyHandler energyHandler = TesseractLookups.ENERGY_HANDLER_SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        if (energyHandler == null) {
            energyHandler = getEnergyStorage(entity, side);
        }
        return Optional.ofNullable(energyHandler);
    }

    @Override
    public Optional<IHeatHandler> getHeatHandler(BlockEntity entity, Direction side){
        IHeatHandler heatHandler = TesseractLookups.HEAT_HANDLER_SIDED.find(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity, side);
        return Optional.ofNullable(heatHandler);
    }

    //TODO figure out better abstraction method
    @Override
    public Optional<PlatformItemHandler> getItemHandler(BlockEntity tile, Direction side){
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, side);
        //if (storage instanceof IItemHandler itemHandler) return Optional.of(itemHandler);
        return storage == null ? Optional.empty() : Optional.of(new FabricPlatformItemHandler(storage));
    }

    @Override
    public Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) return FluidHooks.safeGetBlockFluidManager(blockEntity, side);
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
        return storage == null ? Optional.empty() : Optional.of(new FabricFluidHandler(storage));
    }

    @Override
    public IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, capSide);
        if (storage != null){
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            if (storage instanceof IItemNode node) return node;
            if (storage instanceof ExtendedContainerWrapper wrapper && wrapper.container() instanceof IItemNode node) {
                return node;
            }
            return new ItemStorageWrapper(storage);
        }
        return null;
    }

    private IEnergyHandler getEnergyStorage(BlockEntity be, Direction direction){
        EnergyStorage storage = EnergyStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, direction);
        if (storage == null) return null;
        if (storage instanceof IEnergyHandler moveable1) return moveable1;
        if (storage instanceof IEnergyHandlerStorage handlerStorage) return handlerStorage.getEnergyHandler();
        return new EnergyTileWrapper(be, storage);
    }

    @Override
    public IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        if(tile instanceof FluidHoldingBlock attachment) {
            FluidContainer container = attachment.getFluidContainer().getContainer(capSide);
            if (container == null) return null;
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            if (container instanceof IFluidNode node) return node;
            else return new FluidContainerWrapper(container);
        }
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, capSide);
        if (storage != null){
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            if (storage instanceof IFluidNode node) return node;
            if (storage instanceof FabricBlockFluidContainerAccessor accessor && accessor.getContainer() instanceof IFluidNode node) return node;
            return new FluidTileWrapper(tile, storage);
        }
        return null;
    }
}
