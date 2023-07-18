package tesseract.api.forge.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractConfig;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;

public class EnergyStackWrapper implements IEnergyHandlerItem {
    private final ItemStack stack;
    private final IEnergyStorage storage;

    private final GTConsumer.State state = new GTConsumer.State(this);

    public EnergyStackWrapper(ItemStack stack, IEnergyStorage storage) {
        this.stack = stack;
        this.storage = storage;
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        if (storage.getEnergyStored() >= transaction.voltageOut * TesseractConfig.COMMON.EU_TO_FE_RATIO) {
            transaction.addData(1, 0, this::extractEnergy);
            return true;
        }
        return false;
    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        return storage.extractEnergy((int) (data.getEnergy(1, false) * TesseractConfig.COMMON.EU_TO_FE_RATIO), false) > 0;
    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        return storage.receiveEnergy((int) (data.getEnergy(1, true) * TesseractConfig.COMMON.EU_TO_FE_RATIO), false) > 0;
    }

    @Override
    public GTTransaction extract(GTTransaction.Mode mode) {
        return new GTTransaction(0, 0, a -> {
        });
    }

    @Override
    public long getEnergy() {
        return (long) (storage.getEnergyStored() / TesseractConfig.COMMON.EU_TO_FE_RATIO);
    }

    @Override
    public long getCapacity() {
        return (long) (storage.getMaxEnergyStored() / TesseractConfig.COMMON.EU_TO_FE_RATIO);
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
        return 1;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }

    @Override
    public long availableAmpsInput(long voltage) {
        if (!canInput()) return 0;
        int inserted = storage.receiveEnergy((int) (voltage * TesseractConfig.COMMON.EU_TO_FE_RATIO), false);
        return inserted == voltage ? 1 : 0;
    }

    @Override
    public boolean canOutput() {
        return TesseractConfig.COMMON.ENABLE_FE_OR_TRE_INPUT && storage.canExtract();
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

    @Override
    public void setCapacity(long capacity) {

    }

    @Override
    public void setEnergy(long energy) {

    }

    @Override
    public @NotNull TesseractItemContext getContainer() {
        return new ItemStackWrapper(this.stack);
    }
}