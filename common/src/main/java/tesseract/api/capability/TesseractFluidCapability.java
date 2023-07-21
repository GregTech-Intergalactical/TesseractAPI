package tesseract.api.capability;


import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.FluidSnapshot;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractCapUtils;
import tesseract.TesseractGraphWrappers;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import java.util.List;


public class TesseractFluidCapability<T extends BlockEntity & IFluidPipe> extends TesseractBaseCapability<T> implements IFluidNode {

    private FluidTransaction old;

    public TesseractFluidCapability(T tile, Direction dir, boolean isNode, ITransactionModifier callback) {
        super(tile, dir, isNode, callback);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FluidContainer copy() {
        return null;
    }

    @NotNull
    @Override
    public FluidHolder getFluidInTank(int tank) {
        return FluidHooks.emptyFluid();
    }

    @Override
    public List<FluidHolder> getFluids() {
        return List.of(FluidHooks.emptyFluid());
    }

    @Override
    public long getTankCapacity(int tank) {
        return Integer.MAX_VALUE * TesseractGraphWrappers.dropletMultiplier;
    }

    @Override
    public void fromContainer(FluidContainer container) {

    }

    @Override
    public long extractFromSlot(FluidHolder fluidHolder, FluidHolder toInsert, Runnable snapshot) {
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
    public FluidSnapshot createSnapshot() {
        return null;
    }

    @Override
    public long insertFluid(FluidHolder resource, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (!simulate) {
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            FluidTransaction transaction = new FluidTransaction(resource.copyHolder(), a -> {});
            if (!this.isNode) {
                TesseractGraphWrappers.FLUID.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        this.isSending = false;
        return resource.getFluidAmount() - this.old.stack.getFluidAmount();
    }


    private void transferAroundPipe(FluidTransaction transaction, long pos) {
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            FluidHolder stack = transaction.stack.copyHolder();
            this.callback.modify(stack, this.side, dir, true);
            //Check the handler.
            var cap = TesseractCapUtils.getFluidHandler(tile.getLevel(), BlockPos.of(Pos.offset(pos, dir)), dir.getOpposite());
            if (cap.isEmpty()) continue;
            //Perform insertion, and add to the transaction.
            var handler = cap.get();
            long amount = handler.insertFluid(stack,  true);
            if (amount > 0) {
                stack.setAmount(amount);
                transaction.addData(stack, a -> {
                    this.callback.modify(a, this.side, dir, false);
                    handler.insertFluid(a, false);
                });
            }
            if (transaction.stack.isEmpty()) break;
        }
    }

    @NotNull
    @Override
    public FluidHolder extractFluid(FluidHolder resource, boolean simulate) {
        return FluidHooks.emptyFluid();
    }

    @Override
    public void setFluid(int slot, FluidHolder fluid) {

    }

    @Override
    public int getPriority(Direction direction) {
        return 0;
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
    public boolean canInput(FluidHolder fluid, Direction direction) {
        return true;
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
