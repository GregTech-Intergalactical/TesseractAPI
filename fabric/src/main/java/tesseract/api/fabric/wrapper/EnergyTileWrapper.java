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
    public boolean insert(GTTransaction transaction) {
        if (storage.getAmount() >= transaction.voltageOut * TesseractConfig.COMMON.EU_TO_TRE_RATIO) {
            transaction.addData(1, 0, this::extractEnergy);
            return true;
        }
        return false;
    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        try(Transaction transaction = Transaction.openOuter()) {
            boolean extract = storage.extract((long) (data.getEnergy(1, false) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction) > 0;
            if (extract) transaction.commit();
            return extract;
        }

    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        try(Transaction transaction = Transaction.openOuter()) {
            boolean insert = storage.insert((long) (data.getEnergy(1, true) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), transaction) > 0;
            if (insert) transaction.commit();
            return insert;
        }
    }

    @Override
    public GTTransaction extract(GTTransaction.Mode mode) {
        return new GTTransaction(0, 0, a -> {
        });
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
        return 1;
    }

    @Override
    public long getInputVoltage() {
        if (storage instanceof SimpleSidedEnergyContainer limitingEnergyStorage){
            return limitingEnergyStorage.getMaxInsert(null);
        }
        return 32;
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