package tesseract.mixin.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import team.reborn.energy.api.base.SimpleBatteryItem;
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.fabric.wrapper.ContainerItemContextWrapper;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;

@Mixin(SimpleItemEnergyStorageImpl.class)
public abstract class SimpleItemEnergyStorageImplMixin implements IEnergyHandlerItem {
    @Shadow(remap = false) public abstract long getAmount();

    @Shadow(remap = false) public abstract boolean supportsExtraction();

    @Shadow(remap = false) public abstract boolean supportsInsertion();

    @Shadow(remap = false) @Final private long maxInsert;
    @Shadow(remap = false) @Final private long maxExtract;

    @Shadow(remap = false) public abstract long getCapacity();

    @Shadow(remap = false) public abstract long extract(long maxAmount, TransactionContext transaction);

    @Shadow(remap = false) public abstract long insert(long maxAmount, TransactionContext transaction);

    @Shadow(remap = false) @Final private ContainerItemContext ctx;

    @Shadow(remap = false) protected abstract boolean trySetEnergy(long energyAmountPerCount, long count, TransactionContext transaction);

    @Unique
    protected GTConsumer.State state = new GTConsumer.State(this);

    @Unique
    private TesseractItemContext tesseractContext;

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        return new CompoundTag();
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        if (data.transaction.mode == GTTransaction.Mode.TRANSMIT) {
            long amps = Math.min(data.getAmps(false), this.availableAmpsOutput());
            amps = Math.min(amps, this.getEnergy() / this.getOutputVoltage());
            long toDrain = data.getEnergy(amps, false);
            try(Transaction transaction = Transaction.openOuter()) {
                long drained = this.extract(toDrain, transaction);
                if (drained > 0){
                    transaction.commit();
                    this.getState().extract(false, amps);
                    data.useAmps(false, amps);
                }
                return amps > 0;
            }

        } else {
            long toDrain = Math.min(data.getEu(), this.getEnergy());
            try(Transaction transaction = Transaction.openOuter()) {
                long extracted = this.extract(toDrain, transaction);
                if (extracted > 0) {
                    data.drainEu(toDrain);
                    transaction.commit();
                }
                return extracted > 0;
            }
        }
    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        if (data.transaction.mode == GTTransaction.Mode.TRANSMIT) {
            long amps = Math.min(data.getAmps(true), this.availableAmpsInput(data.getVoltage()));
            amps = Math.min(amps, (this.getCapacity() - this.getAmount()) / this.getInputVoltage());
            long toAdd = data.getEnergy(amps, true);
            try(Transaction transaction = Transaction.openOuter()) {
                long added = this.insert(toAdd, transaction);
                if (added > 0){
                    transaction.commit();
                    data.useAmps(true, amps);
                    this.getState().receive(false, amps);
                }
                return amps > 0;
            }

        } else {
            long toAdd = Math.min(data.getEu(), this.getCapacity() - this.getAmount());
            try(Transaction transaction = Transaction.openOuter()) {
                long inserted = this.insert(toAdd, transaction);
                if (inserted > 0) {
                    data.drainEu(toAdd);
                    transaction.commit();
                }
                return inserted > 0;
            }
        }
    }


    @Override
    public long availableAmpsInput(long voltage) {
        long added = 0;
        try(Transaction transaction = Transaction.openOuter()) {
            added = this.insert(voltage, transaction);
        }
        if (added == voltage) return 1;
        return 0;
    }

    @Override
    public long getEnergy() {
        return getAmount();
    }

    @Override
    public long getOutputAmperage() {
        return 1;
    }

    @Override
    public long getOutputVoltage() {
        return this.maxExtract;
    }

    @Override
    public long getInputAmperage() {
        return 2;
    }

    @Override
    public long getInputVoltage() {
        return this.maxInsert;
    }

    @Override
    public boolean canOutput() {
        return supportsExtraction();
    }

    @Override
    public boolean canInput() {
        return supportsInsertion();
    }

    @Override
    public boolean canInput(Direction direction) {
        return supportsInsertion();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return supportsExtraction();
    }

    @Override
    public GTConsumer.State getState() {
        return state;
    }

    @Override
    public void setEnergy(long energy) {
        if (energy < 0) return;
        ItemStack newStack = ctx.getItemVariant().toStack((int) ctx.getAmount());
        SimpleBatteryItem.setStoredEnergyUnchecked(newStack, energy);
        getContainer().setItemStack(newStack);
    }

    @Override
    public void setCapacity(long capacity) {
        // nothing cause the capacity is not mutable.
    }

    @Override
    public @NotNull TesseractItemContext getContainer() {
        if (tesseractContext == null) tesseractContext = new ContainerItemContextWrapper(ctx);
        return tesseractContext;
    }
}
