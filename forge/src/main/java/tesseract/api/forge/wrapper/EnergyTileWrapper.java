package tesseract.api.forge.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.TesseractConfig;
import tesseract.api.Transaction;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public class EnergyTileWrapper implements IEnergyHandler {

    private final BlockEntity tile;
    private final IEnergyStorage storage;

    private final GTConsumer.State state = new GTConsumer.State(this);

    public EnergyTileWrapper(BlockEntity tile, IEnergyStorage storage) {
        this.tile = tile;
        this.storage = storage;
    }

    @Override
    public long insertAmps(long voltage, long amps, boolean simulate) {
        long inserted = storage.receiveEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), simulate);
        if (inserted == voltage * TesseractConfig.EU_TO_FE_RATIO.get()){
            return 1;
        }
        return 0;

    }

    @Override
    public long extractAmps(long voltage, long amps, boolean simulate) {
        long inserted = storage.extractEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), simulate);
        if (inserted == voltage * TesseractConfig.EU_TO_FE_RATIO.get()){
            return 1;
        }
        return 0;

    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        return (long) (storage.receiveEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), simulate) / TesseractConfig.EU_TO_FE_RATIO.get());

    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return (long) (storage.extractEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), simulate) / TesseractConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long getEnergy() {
        return (long) (storage.getEnergyStored() / TesseractConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long getCapacity() {
        return (long) (storage.getMaxEnergyStored() / TesseractConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long availableAmpsInput(long voltage) {
        if (!canInput()) return 0;
        int inserted = storage.receiveEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), false);
        return inserted == voltage ? 1 : 0;
    }

    @Override
    public long getOutputAmperage() {
        return 1;
    }

    @Override
    public long getOutputVoltage() {
        return 32;
    }

    @Override
    public long getInputAmperage() {
        return 16;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }

    @Override
    public boolean canOutput() {
        return TesseractConfig.ENABLE_FE_OR_TRE_INPUT.get() && storage.canExtract();
    }

    @Override
    public boolean canInput() {
        return storage.canReceive();
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
    public void deserialize(CompoundTag arg) {

    }
}