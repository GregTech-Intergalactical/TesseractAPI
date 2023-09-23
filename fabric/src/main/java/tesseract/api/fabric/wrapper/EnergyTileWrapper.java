package tesseract.api.fabric.wrapper;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;
import tesseract.TesseractConfig;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public class EnergyTileWrapper implements IEnergyHandler {

    private final BlockEntity tile;
    private final EnergyStorage storage;

    private final GTConsumer.State state = new GTConsumer.State(this);

    public EnergyTileWrapper(BlockEntity tile, EnergyStorage storage) {
        this.tile = tile;
        this.storage = storage;
    }

    @Override
    public long insertAmps(long voltage, long amps, boolean simulate) {
        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert((long) (voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction);
            if (inserted == voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO){
                if (!simulate) transaction.commit();
                return 1;
            }
            return 0;
        }

    }

    @Override
    public long extractAmps(long voltage, long amps, boolean simulate) {
        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.extract((long) (voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction);
            if (inserted == voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO){
                if (!simulate) transaction.commit();
                return 1;
            }
            return 0;
        }

    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = (long) (storage.insert((long) (voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction) / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
            if (!simulate) transaction.commit();
            return inserted;
        }

    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = (long) (storage.extract((long) (voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction) / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    @Override
    public long getEnergy() {
        return (long) (storage.getAmount() / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    public long getCapacity() {
        return (long) (storage.getCapacity() / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    public long getOutputAmperage() {
        return 1;
    }

    @Override
    public long getOutputVoltage() {
        if (storage instanceof SimpleSidedEnergyContainer limitingEnergyStorage){
            return limitingEnergyStorage.getMaxExtract(null);
        }
        return 32;
    }

    @Override
    public long getInputAmperage() {
        return 16;
    }

    @Override
    public long getInputVoltage() {
        if (storage instanceof SimpleSidedEnergyContainer limitingEnergyStorage){
            return limitingEnergyStorage.getMaxInsert(null);
        }
        return 8192;
    }

    @Override
    public long availableAmpsInput(long voltage) {
        long added = 0;
        try(Transaction transaction = Transaction.openOuter()) {
            added = storage.insert((long) (voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction);
        }
        if (added == voltage * TesseractConfig.COMMON.EU_TO_TRE_RATIO) return 1;
        return 0;
    }

    @Override
    public boolean canOutput() {
        return TesseractConfig.COMMON.ENABLE_FE_OR_TRE_INPUT && storage.supportsExtraction();
    }

    @Override
    public boolean canInput() {
        return storage.supportsInsertion();
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