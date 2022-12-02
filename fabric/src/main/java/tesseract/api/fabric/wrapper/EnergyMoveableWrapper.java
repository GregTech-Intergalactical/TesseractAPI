package tesseract.api.fabric.wrapper;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.api.energy.EnergyMoveable;
import aztech.modern_industrialization.compat.megane.holder.EnergyComponentHolder;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public class EnergyMoveableWrapper implements IEnergyHandler {
    private final BlockEntity blockEntity;
    private final EnergyMoveable storage;

    private final GTConsumer.State state = new GTConsumer.State(this);

    public EnergyMoveableWrapper(BlockEntity blockEntity, EnergyMoveable storage) {
        this.storage = storage;
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        if (getEnergy() >= transaction.voltageOut) {
            transaction.addData(1, 0, this::extractEnergy);
            return true;
        }
        return false;
    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        if (storage instanceof EnergyExtractable insertable){
            return insertable.canExtract(getTier(data.getVoltage())) && insertable.extractEnergy(data.getEnergy(1, true), Simulation.ACT) > 0;
        }
        return false;
    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        if (storage instanceof EnergyInsertable insertable){
            return insertable.canInsert(getTier(data.getVoltage())) && insertable.insertEnergy(data.getEnergy(1, true), Simulation.ACT) > 0;
        }
        return false;
    }

    @Override
    public GTTransaction extract(GTTransaction.Mode mode) {
        return new GTTransaction(0, 0, a -> {
        });
    }

    public CableTier getTier(long tier){
        if (tier <= 32){
            return CableTier.LV;
        } else if (tier <= 128){
            return CableTier.MV;
        } else if (tier <= 1024){
            return CableTier.HV;
        } else if (tier < 8192){
            return CableTier.EV;
        }
        return CableTier.SUPERCONDUCTOR;
    }

    @Override
    public long getEnergy() {
        if (blockEntity instanceof EnergyComponentHolder holder){
            return holder.getEnergyComponent().getEu();
        }
        return 0;
    }

    @Override
    public long getCapacity() {
        if (blockEntity instanceof EnergyComponentHolder holder){
            return holder.getEnergyComponent().getCapacity();
        }
        return 0;
    }

    @Override
    public long getOutputAmperage() {
        return 1;
    }

    @Override
    public long getOutputVoltage() {
        if (blockEntity instanceof IEnergyMoveableTiers tiers) return tiers.getOutputVoltage();
        return 32;
    }

    @Override
    public long getInputAmperage() {
        return 1;
    }

    @Override
    public long getInputVoltage() {
        if (blockEntity instanceof IEnergyMoveableTiers tiers) return tiers.getInputVoltage();
        return 32;
    }

    @Override
    public boolean canOutput() {
        return storage instanceof EnergyExtractable;
    }

    @Override
    public boolean canInput() {
        return storage instanceof EnergyInsertable;
    }

    @Override
    public boolean canInput(Direction dir) {
        return canInput();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return canOutput();
    }

    @Override
    public GTConsumer.State getState() {
        return state;
    }

    @Override
    public void tesseractTick() {
        getState().onTick();
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
