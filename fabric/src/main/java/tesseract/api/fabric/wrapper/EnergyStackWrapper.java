package tesseract.api.fabric.wrapper;

import earth.terrarium.botarium.common.energy.base.PlatformItemEnergyManager;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractConfig;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandlerItem;

public class EnergyStackWrapper implements IEnergyHandlerItem {
    private final ItemStackHolder holder;
    private final PlatformItemEnergyManager storage;

    private final GTConsumer.State state = new GTConsumer.State(this);

    public EnergyStackWrapper(ItemStack stack, PlatformItemEnergyManager storage) {
        this.holder = new ItemStackHolder(stack);
        this.storage = storage;
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        return (long) (storage.insert(holder, (long) (voltage * TesseractConfig.EU_TO_TRE_RATIO.get()), simulate) / TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return (long) (storage.extract(holder, (long) (voltage * TesseractConfig.EU_TO_TRE_RATIO.get()), simulate) / TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    public long getEnergy() {
        return (long) (storage.getStoredEnergy() / TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    public long getCapacity() {
        return (long) (storage.getCapacity() / TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    public long availableAmpsInput(long voltage) {
        if (!canInput()) return 0;
        long inserted = storage.insert(holder, (long) (voltage * TesseractConfig.EU_TO_TRE_RATIO.get()), false);
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
        return 1;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }

    @Override
    public boolean canOutput() {
        return TesseractConfig.ENABLE_FE_OR_TRE_INPUT.get() && storage.supportsExtraction();
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
        return new TesseractItemContext() {
            @Override
            public Item getItem() {
                return holder.getStack().getItem();
            }

            @Override
            public @NotNull CompoundTag getTag() {
                return holder.getStack().getTag();
            }

            @Override
            public int getCount() {
                return holder.getStack().getCount();
            }

            @Override
            public void setItemStack(ItemStack stack) {

            }
        };
    }
}