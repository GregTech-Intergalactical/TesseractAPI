package tesseract.api.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import tesseract.Tesseract;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.graph.Graph;
import tesseract.util.Pos;

public class TesseractGTCapability<T extends BlockEntity & IGTCable> extends TesseractBaseCapability<T> implements IGTNode, INBTSerializable<CompoundTag> {

    private final IGTCable cable;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = tile;
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        boolean flag = false;
        if (this.isSending) return false;
        this.isSending = true;
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            long old = transaction.getAvailableAmps();
            Tesseract.getGT_ENERGY().getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            flag = transaction.getAvailableAmps() < old;
        } else {
            for (Direction dir : Graph.DIRECTIONS) {
                if (dir == side || !this.tile.connects(dir)) continue;
                Tesseract.getGT_ENERGY().getController(tile.getLevel(), pos).insert(Pos.offset(pos, dir), dir.getOpposite(), transaction, callback);
            }
        }
        this.isSending = false;
        return flag;
    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        return false;
    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        return false;
    }

    @Override
    public GTTransaction extract(GTTransaction.Mode mode) {
        return new GTTransaction(0, 0, a -> {
        });
    }

    @Override
    public long getEnergy() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    

    @Override
    public long availableAmpsInput() {
        return Long.MAX_VALUE;
    }

    @Override
    public long availableAmpsOutput() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getOutputAmperage() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getOutputVoltage() {
        return cable.getVoltage();
    }

    @Override
    public long getInputAmperage() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputVoltage() {
        return cable.getVoltage();
    }

    @Override
    public boolean canOutput() {
        return true;
    }

    @Override
    public boolean canInput() {
        return true;
    }

    @Override
    public boolean canInput(Direction direction) {
        return true;
    }

    @Override
    public boolean canOutput(Direction direction) {
        return true;
    }

    @Override
    public GTConsumer.State getState() {
        return new GTConsumer.State(this);
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
