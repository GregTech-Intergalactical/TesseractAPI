package tesseract.api.forge.wrapper;

import earth.terrarium.botarium.api.energy.EnergySnapshot;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.api.rf.IRFNode;

public record RFWrapper(IEnergyStorage storage) implements IRFNode {

    @Override
    public long insertEnergy(long maxAmount, boolean simulate) {
        return storage.receiveEnergy((int) maxAmount, simulate);
    }

    @Override
    public long extractEnergy(long maxAmount, boolean simulate) {
        return storage.extractEnergy((int) maxAmount, simulate);
    }

    @Override
    public void setEnergy(long energy) {
        storage.extractEnergy(storage.getEnergyStored(), false);
        storage.receiveEnergy((int) energy, false);
    }

    @Override
    public long getStoredEnergy() {
        return storage.getEnergyStored();
    }

    @Override
    public long getMaxCapacity() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public long maxInsert() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long maxExtract() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean allowsInsertion() {
        return storage.canReceive();
    }

    @Override
    public boolean allowsExtraction() {
        return storage.canExtract();
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
        return allowsInsertion();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return allowsExtraction();
    }
}
