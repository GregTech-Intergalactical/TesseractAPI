package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractCapUtils;
import tesseract.TesseractGraphWrappers;
import tesseract.api.gt.*;
import tesseract.graph.Graph;
import tesseract.util.Pos;

public class TesseractGTCapability<T extends BlockEntity & IGTCable> extends TesseractBaseCapability<T> implements IEnergyHandler {

    private final IGTCable cable;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = tile;
    }

    @Override
    public long insertAmps(long voltage, long amps, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        GTDataHolder dataHolder = new GTDataHolder(new Tuple<>(voltage, amps), 0L);
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            TesseractGraphWrappers.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, dataHolder, callback, simulate);
        } else {
            transferAroundPipe(dataHolder, pos, simulate);
        }
        this.isSending = false;
        return dataHolder.getData();
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        return insertAmps(voltage, 1, simulate) == 1 ? voltage : 0;
    }

    @Override
    public long extractAmps(long voltage, long amps, boolean simulate) {
        return 0;
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return 0;
    }

    private void transferAroundPipe(GTDataHolder transaction, long pos, boolean simulate) {
        boolean flag = false;
        long availableAmps = transaction.getImmutableData().getB();
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
                long voltage = transaction.getImmutableData().getA() - cable.getLoss();
                long ampsToInsert = handler.availableAmpsInput(voltage);
                if (this.callback.modify(transaction, this.side, dir, simulate)) continue;
                long amps = handler.insertAmps(voltage, ampsToInsert, simulate);
                if (amps > 0){
                    transaction.setData(transaction.getData() + amps);
                    availableAmps -= amps;
                }
                if (availableAmps == 0) break;

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
