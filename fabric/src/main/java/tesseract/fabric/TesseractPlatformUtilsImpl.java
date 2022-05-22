package tesseract.fabric;

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
        // client handling
        if (Objects.requireNonNull(be.getLevel()).isClientSide()) {
            return LazyOptional.empty();
        }
        // external handling
        List<Storage<ItemVariant>> itemStorages = new ArrayList<>();
        Level l = be.getLevel();
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();

        for (Direction direction : getDirections(side)) {
            Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(l, pos, state, be, direction);

            if (itemStorage != null) {
                if (itemStorages.size() == 0) {
                    itemStorages.add(itemStorage);
                    continue;
                }

                for (Storage<ItemVariant> storage : itemStorages) {
                    if (!Objects.equals(itemStorage, storage)) {
                        itemStorages.add(itemStorage);
                        break;
                    }
                }
            }
        }


        if (itemStorages.isEmpty()) return LazyOptional.empty();
        if (itemStorages.size() == 1) return simplifyItem(itemStorages.get(0));
        return simplifyItem(new CombinedStorage<>(itemStorages));
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
        // client handling
        if (client) { // TODO this system might be unnecessary
//            IFluidHandler cached = FluidTileDataHandler.getCachedHandler(be);
//            return LazyOptional.ofObject(cached);
        }
        // external handling
        List<Storage<FluidVariant>> fluidStorages = new ArrayList<>();
        Level l = be.getLevel();
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();

        for (Direction direction : getDirections(side)) {
            Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(l, pos, state, be, direction);

            if (fluidStorage != null) {
                if (fluidStorages.size() == 0) {
                    fluidStorages.add(fluidStorage);
                    continue;
                }

                for (Storage<FluidVariant> storage : fluidStorages) {
                    if (!Objects.equals(fluidStorage, storage)) {
                        fluidStorages.add(fluidStorage);
                        break;
                    }
                }
            }
        }

        if (fluidStorages.isEmpty()) return LazyOptional.empty();
        if (fluidStorages.size() == 1) return simplifyFluid(fluidStorages.get(0));
        return simplifyFluid(new CombinedStorage<>(fluidStorages));
    }

    // Fluid-containing items

    public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return LazyOptional.empty();
        ContainerItemContext ctx = ContainerItemContext.withInitial(stack);
        Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ctx);
        return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(ctx, fluidStorage));
    }

    private static Direction[] getDirections(@Nullable Direction direction) {
        if (direction == null) return Direction.values();
        return new Direction[] {direction};
    }

    public static LazyOptional<IItemHandler> simplifyItem(Storage<ItemVariant> storage) {
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof ItemHandlerStorage handler) return LazyOptional.ofObject(handler.getHandler());
        return LazyOptional.ofObject(new ItemStorageHandler(storage));
    }

    public static LazyOptional<IFluidHandler> simplifyFluid(Storage<FluidVariant> storage) {
        if (storage == null) return LazyOptional.empty();
        if (storage instanceof FluidHandlerStorage handler) return LazyOptional.ofObject(handler.handler());
        return LazyOptional.ofObject(new FluidStorageHandler(storage));
    }

    public static ResourceLocation getFluidId(Fluid fluid){
        return Registry.FLUID.getKey(fluid);
    }

    public static int getFluidTemperature(Fluid fluid){
        return 0;
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return false;
    }
}
