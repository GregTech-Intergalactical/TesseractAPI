package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractCapUtils;
import tesseract.TesseractGraphWrappers;
import tesseract.api.gt.*;
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
        if (this.isSending || (!simulate && old == null)) return 0;
        this.isSending = true;
        if (!simulate) {
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
        this.isSending = false;
        return voltage - old.eu;
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return 0;
    }

    private void transferAroundPipe(GTTransaction transaction, long pos) {
        boolean hasInserted = false;
        boolean lossAdded = false;
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
                if (hasInserted && !lossAdded){
                    transaction.addData(0, loss, 0, d -> {});
                    lossAdded = true;
                }

                long remainingEu = lossAdded ? transaction.eu : transaction.eu - loss;
                GTTransaction.TransferData data = new GTTransaction.TransferData(transaction, remainingEu, transaction.voltage).setLoss(cable.getLoss());
                if (this.callback.modify(data, dir, false, true) || this.callback.modify(data, side, true, true)){
                    continue;
                }
                if (data.getEu() < remainingEu) remainingEu = data.getEu();
                if (data.getLoss() > 0) remainingEu -= Math.round(data.getLoss());
                if (remainingEu <= 0) return;
                long inserted = handler.insertEu(remainingEu, true);
                if (inserted > 0){
                    transaction.addData(inserted, inserted, cable.getLoss(), t -> {
                        if (this.callback.modify(t, dir, false, false) || this.callback.modify(data, side, true, false)){
                            return;
                        }
                        handler.insertEu(t.getEu(), false);
                    });
                    if (transaction.voltage > this.cable.getVoltage()){
                        ((IGTEvent)TesseractGraphWrappers.GT_ENERGY.getController(tile.getLevel(), pos)).onCableOverVoltage(tile.getLevel(), pos, transaction.voltage);
                    }
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
