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
    public long extractAmps(long voltage, long amps, boolean simulate) {
        if (storage instanceof EnergyExtractable extractable && extractable.canExtract(getTier(voltage))){
            if (extractable.extractEnergy(voltage, Simulation.SIMULATE) == voltage){
                if (!simulate) extractable.extractEnergy(voltage, Simulation.ACT);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        if (storage instanceof EnergyExtractable extractable && extractable.canExtract(getTier(voltage))){
            return extractable.extractEnergy(voltage, simulate ? Simulation.SIMULATE : Simulation.ACT);
        }
        return 0;
    }

    @Override
    public long insertAmps(long voltage, long amps, boolean simulate) {
        if (storage instanceof EnergyInsertable insertable && insertable.canInsert(getTier(voltage))){
            if (insertable.insertEnergy(voltage, Simulation.SIMULATE) == voltage){
                if (!simulate) insertable.insertEnergy(voltage, Simulation.ACT);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        if (storage instanceof EnergyInsertable insertable && insertable.canInsert(getTier(voltage))){
            return insertable.insertEnergy(voltage, simulate ? Simulation.SIMULATE : Simulation.ACT);
        }
        return 0;
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
    public CompoundTag serialize(CompoundTag tag) {
        return null;
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }
}
