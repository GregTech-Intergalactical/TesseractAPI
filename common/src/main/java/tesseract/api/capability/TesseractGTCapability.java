package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractCapUtils;
import tesseract.TesseractGraphWrappers;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTCable;
import tesseract.graph.Graph;
import tesseract.util.Pos;

public class TesseractGTCapability<T extends BlockEntity & IGTCable> extends TesseractBaseCapability<T> implements IEnergyHandler {

    private final IGTCable cable;
    private GTTransaction old;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = tile;
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (!simulate) {
            if (old == null) return 0;
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            GTTransaction transaction = new GTTransaction(voltage, t -> {});
            if (!this.isNode) {
                TesseractGraphWrappers.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        return voltage - old.eu;
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return 0;
    }

    private void transferAroundPipe(GTTransaction transaction, long pos) {
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            //First, perform cover modifications.
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                //Check the handler.
                var cap = TesseractCapUtils.getEnergyHandler(otherTile, dir.getOpposite());
                if (cap.isEmpty()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.get();
                long loss = Math.round(cable.getLoss());
                long voltage = transaction.voltage - loss;
                long remainingEu = transaction.eu;
                long toInsert = Math.min(remainingEu, voltage);
                GTTransaction.TransferData data = new GTTransaction.TransferData(transaction, toInsert, transaction.voltage).setLoss(cable.getLoss());
                if (this.callback.modify(data, dir, false, true) || this.callback.modify(data, side, true, true)){
                    continue;
                }
                if (data.getEu() < toInsert) toInsert = data.getEu();
                if (data.getLoss() > 0) toInsert -= Math.round(data.getLoss());
                if (toInsert <= 0) return;
                long inserted = handler.insertEu(toInsert, true);
                if (inserted > 0){
                    transaction.addData(inserted, cable.getLoss(), t -> {
                        if (this.callback.modify(t, dir, false, false) || this.callback.modify(data, side, true, false)){
                            return;
                        }
                        handler.insertEu(t.getEu(), false);
                    });
                }
                if (transaction.eu == 0) break;
            }
        }
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
    public long availableAmpsInput(long voltage) {
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
    public CompoundTag serialize(CompoundTag tag) {
        return null;
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }
}
