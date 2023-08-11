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
    public long insertAmps(long voltage, long amps, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (!simulate) {
            if (old == null) return 0;
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            GTTransaction transaction = new GTTransaction(amps, voltage, t -> {});
            if (!this.isNode) {
                TesseractGraphWrappers.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        return amps - old.getAvailableAmps();
    }

    @Override
    public long insertEu(long energy, boolean simulate) {
        return 0;
    }

    @Override
    public long extractAmps(long voltage, long amps, boolean simulate) {
        return 0;
    }

    @Override
    public long extractEu(long energy, boolean simulate) {
        return 0;
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        boolean flag = false;
        if (this.isSending) return false;
        this.isSending = true;
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            long old = transaction.getAvailableAmps();
            TesseractGraphWrappers.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            flag = transaction.getAvailableAmps() < old;
        } else {
            //flag = transferAroundPipe(transaction, pos);
        }
        this.isSending = false;
        return flag;
    }
    private void transferAroundPipe(GTTransaction transaction, long pos) {
        boolean flag = false;
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            //First, perform cover modifications.
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                //Check the handler.
                var cap = TesseractCapUtils.getEnergyHandler(otherTile, dir.getOpposite());
                if (!cap.isPresent()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.get();
                long voltage = transaction.voltageOut - cable.getLoss();
                long ampsToInsert = handler.availableAmpsInput(voltage);
                this.callback.modify(new GTTransaction.TransferData(transaction,voltage, ampsToInsert), this.side, dir, true);
                long amps = handler.insertAmps(voltage, ampsToInsert, true);
                if (amps > 0){
                    transaction.addData(amps, cable.getLoss(), t -> {
                        callback.modify(t, this.side, dir, false);
                        handler.insertAmps(t.getVoltage(), t.getAmps(true), false);
                    });
                }
                if (transaction.getAvailableAmps() == 0) break;

            }
        }
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
