package tesseract.api.fabric.wrapper;


import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.FluidSnapshot;
import earth.terrarium.botarium.fabric.fluid.holder.FabricFluidHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record FluidTileWrapper(BlockEntity tile,
                               Storage<FluidVariant> storage) implements IFluidNode {

    @Override
    public int getPriority(Direction direction) {
        return (!(storage instanceof IFluidNode) ? 0 : ((IFluidNode) storage).getPriority(direction));
    }

    @Override
    public boolean allowsExtraction() {
        return storage().supportsExtraction();
    }

    @Override
    public FluidSnapshot createSnapshot() {
        return null;
    }

    @Override
    public boolean allowsInsertion() {
        return storage.supportsInsertion();
    }

    @Override
    public boolean canInput(Direction direction) {
        return ((!(storage instanceof IFluidNode) && storage.supportsInsertion()) || ((IFluidNode) storage).canInput(direction));
    }

    @Override
    public boolean canOutput(Direction direction) {
        return ((!(storage instanceof IFluidNode) && storage.supportsExtraction()) || ((IFluidNode) storage).canOutput(direction));
    }

    @Override
    public boolean canInput(FluidHolder fluid, Direction direction) {
        return ((!(storage instanceof IFluidNode) && storage.supportsInsertion()) || ((IFluidNode) storage).canInput(fluid, direction));
    }

    @Override
    public int getSize() {
        int size = 0;
        for (StorageView<FluidVariant> ignored : storage) {
            size++;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return getFluids().isEmpty() || getFluids().stream().allMatch(FluidHolder::isEmpty);
    }

    @Override
    public FluidContainer copy() {
        return null;
    }

    @Override
    public long getTankCapacity(int tank) {
        List<StorageView<FluidVariant>> fluids = new ArrayList<>();
        try (Transaction transaction = Transaction.openOuter()) {
            storage.iterator().forEachRemaining(fluids::add);
            transaction.abort();
        }

        return fluids.get(tank).getCapacity();
    }

    @Override
    public void fromContainer(FluidContainer container) {

    }

    @Override
    public long extractFromSlot(FluidHolder fluidHolder, FluidHolder toInsert, Runnable snapshot) {
        return 0;
    }

    @Override
    public List<FluidHolder> getFluids() {
        List<FluidHolder> fluids = new ArrayList<>();
        try (Transaction transaction = Transaction.openOuter()) {
            storage.iterator().forEachRemaining(variant -> fluids.add(FabricFluidHolder.of(variant.getResource(), variant.getAmount())));
            transaction.abort();
        }
        return fluids;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidHolder stack) {
        return true;
    }

    @Override
    public long insertFluid(FluidHolder fluid, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            FabricFluidHolder fabricFluidHolder = FabricFluidHolder.of(fluid);
            long inserted = storage.insert(fabricFluidHolder.toVariant(), fabricFluidHolder.getAmount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted;
        }
    }

    @Override
    public FluidHolder extractFluid(FluidHolder fluid, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            FabricFluidHolder fabricFluidHolder = FabricFluidHolder.of(fluid);
            long extracted = storage.extract(fabricFluidHolder.toVariant(), fabricFluidHolder.getAmount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted == 0 ? FabricFluidHolder.of(fabricFluidHolder.toVariant(), extracted) : fluid;
        }
    }

    @Override
    public void setFluid(int slot, FluidHolder fluid) {

    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }

    @Override
    public void clearContent() {

    }
}
