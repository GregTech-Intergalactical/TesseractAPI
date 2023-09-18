package tesseract.api.capability;

import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.util.EnergyHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractGraphWrappers;
import tesseract.api.rf.IRFCable;
import tesseract.api.rf.IRFNode;
import tesseract.api.rf.RFDataHolder;
import tesseract.graph.Graph;
import tesseract.util.Pos;

public class TesseractRFCapability<T extends BlockEntity & IRFCable> extends TesseractBaseCapability<T> implements IRFNode {
    public TesseractRFCapability(T tile, Direction side, boolean isNode, ITransactionModifier callback) {
        super(tile, side, isNode, callback);
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
    public long insertEnergy(long maxAmount, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        RFDataHolder dataHolder = new RFDataHolder(maxAmount, 0L);
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            TesseractGraphWrappers.RF.getController(tile.getLevel(), pos).insert(pos, side, dataHolder, callback, simulate);
        } else {
            transferAroundPipe(dataHolder, pos, simulate);
        }
        this.isSending = false;
        return dataHolder.getData();
    }

    private void transferAroundPipe(RFDataHolder transaction, long pos, boolean simulate) {
        long rf = transaction.getImmutableData();
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                long oldRf = transaction.getData();
                if (this.callback.modify(transaction, this.side, dir, simulate)) continue;
                long newRF = transaction.getData();
                if (newRF > oldRf){
                    rf -= (newRF - oldRf);
                }
                if (rf<= 0) break;
                //Check the handler.
                var cap = EnergyHooks.safeGetBlockEnergyManager(otherTile, dir.getOpposite());
                if (cap.isEmpty()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.get();
                long amount = handler.insert(rf,  simulate);
                if (amount > 0) {
                    rf -= amount;
                    transaction.setData(transaction.getData() + amount);
                }
                if (rf <= 0) break;
            }
        }
    }

    @Override
    public long extractEnergy(long maxAmount, boolean simulate) {
        return 0;
    }

    @Override
    public void setEnergy(long energy) {

    }

    @Override
    public long getStoredEnergy() {
        return 0;
    }

    @Override
    public long getMaxCapacity() {
        return 0;
    }

    @Override
    public long maxInsert() {
        return 0;
    }

    @Override
    public long maxExtract() {
        return 0;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    @Override
    public EnergySnapshot createSnapshot() {
        return null;
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }

    @Override
    public void clearContent() {

    }
}
