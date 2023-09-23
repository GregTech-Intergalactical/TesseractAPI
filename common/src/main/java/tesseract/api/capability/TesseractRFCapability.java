package tesseract.api.capability;


import earth.terrarium.botarium.api.energy.EnergyHooks;
import earth.terrarium.botarium.api.energy.EnergySnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractGraphWrappers;
import tesseract.api.rf.IRFCable;
import tesseract.api.rf.IRFNode;
import tesseract.api.rf.RFTransaction;
import tesseract.graph.Graph;
import tesseract.util.Pos;

public class TesseractRFCapability<T extends BlockEntity & IRFCable> extends TesseractBaseCapability<T> implements IRFNode {
    private RFTransaction old;
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
        if (!simulate) {
            if (old == null) return 0;
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            RFTransaction transaction = new RFTransaction(maxAmount, a -> {});
            if (!this.isNode) {
                TesseractGraphWrappers.RF.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        this.isSending = false;
        return maxAmount - old.rf;
    }

    private void transferAroundPipe(RFTransaction transaction, long pos) {
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                long rf = transaction.rf;
                if (this.callback.modify(rf, this.side, dir, true)) continue;
                //Check the handler.
                var cap = EnergyHooks.safeGetBlockEnergyManager(otherTile, dir.getOpposite());
                if (cap.isEmpty()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.get();
                long amount = handler.insert(rf,  true);
                if (amount > 0) {
                    transaction.addData(rf, a -> {
                        if (this.callback.modify(a, this.side, dir, false)) return;
                        handler.insert(a, false);
                    });
                }
                if (transaction.rf == 0) break;
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
}
