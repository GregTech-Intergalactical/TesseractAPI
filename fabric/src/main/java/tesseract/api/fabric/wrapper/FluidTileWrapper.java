package tesseract.api.fabric.wrapper;

import earth.terrarium.botarium.api.fluid.FluidHolder;
import earth.terrarium.botarium.fabric.fluid.FabricFluidHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
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
    public boolean supportsExtraction() {
        return storage().supportsExtraction();
    }

    @Override
    public boolean supportsInsertion() {
        return supportsInsertion();
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
    public int getTankAmount() {
        int size = 0;
        for (StorageView<FluidVariant> ignored : storage) {
            size++;
        }
        return size;
    }

    @Nonnull
    @Override
    public FluidHolder getFluidInTank(int tank) {
        List<FluidHolder> fluids = new ArrayList<>();
        storage.iterator().forEachRemaining(variant -> fluids.add(FabricFluidHolder.of(variant.getResource(), variant.getAmount())));
        return fluids.get(tank);
    }

    @Override
    public long getTankCapacity(int tank) {
        List<StorageView<FluidVariant>> fluids = new ArrayList<>();
        storage.iterator().forEachRemaining(fluids::add);
        return fluids.get(tank).getCapacity();
    }

    @Override
    public List<FluidHolder> getFluidTanks() {
        List<FluidHolder> fluids = new ArrayList<>();
        storage.iterator().forEachRemaining(variant -> fluids.add(FabricFluidHolder.of(variant.getResource(), variant.getAmount())));
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
}
