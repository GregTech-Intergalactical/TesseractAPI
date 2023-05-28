package tesseract.api.fabric.wrapper;

import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import team.reborn.energy.api.EnergyStorage;
import tesseract.api.rf.IRFNode;

public record RFWrapper(EnergyStorage storage) implements IRFNode {
    @Override
    public long extractEnergy(long amount, boolean simulate) {
        try (Transaction txn = Transaction.openOuter()) {
            long extract = storage.extract(amount, txn);
            if(simulate) txn.abort();
            else txn.commit();
            return extract;
        }
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        try (Transaction txn = Transaction.openOuter()) {
            long insert = storage.insert(amount, txn);
            if(simulate) txn.abort();
            else txn.commit();
            return insert;
        }
    }

    @Override
    public void setEnergy(long energy) {

    }

    @Override
    public long getStoredEnergy() {
        return storage.getAmount();
    }

    @Override
    public long getMaxCapacity() {
        return storage.getCapacity();
    }

    @Override
    public long maxInsert() {
        return Long.MAX_VALUE;
    }

    @Override
    public long maxExtract() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean allowsInsertion() {
        return storage.supportsInsertion();
    }

    @Override
    public boolean allowsExtraction() {
        return storage.supportsExtraction();
    }

    @Override
    public EnergySnapshot createSnapshot() {
        return null;
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }

    @Override
    public boolean canInput(Direction direction) {
        return storage.supportsInsertion();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return storage.supportsExtraction();
    }

    @Override
    public void clearContent() {

    }
}
