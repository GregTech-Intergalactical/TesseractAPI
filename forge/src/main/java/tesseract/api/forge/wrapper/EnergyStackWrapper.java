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
        int inserted = storage.receiveEnergy((int) (voltage * TesseractConfig.EU_TO_FE_RATIO.get()), false);
        return inserted == voltage ? 1 : 0;
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