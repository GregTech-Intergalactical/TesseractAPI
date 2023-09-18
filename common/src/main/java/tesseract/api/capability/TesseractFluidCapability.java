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
import tesseract.api.fluid.*;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import java.util.List;


public class TesseractFluidCapability<T extends BlockEntity & IFluidPipe> extends TesseractBaseCapability<T> implements IFluidNode, FluidContainerHandler {

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
    public FluidContainer getFluidContainer() {
        return this;
    }

    @Override
    public long insertFluid(FluidHolder resource, boolean simulate) {
        if (this.isSending) return 0;
        this.isSending = true;
        FluidDataHolder fluidDataHolder = new FluidDataHolder(resource.copyHolder(), 0L);
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            TesseractGraphWrappers.FLUID.getController(tile.getLevel(), pos).insert(pos, side, fluidDataHolder, callback, simulate);
        } else {
            transferAroundPipe(fluidDataHolder, pos, simulate);
        }
        this.isSending = false;
        return fluidDataHolder.getData();
    }


    private void transferAroundPipe(FluidDataHolder transaction, long pos, boolean simulate) {
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            FluidHolder stack = transaction.getImmutableData();
            if (stack.isEmpty()) break;
            if (this.callback.modify(stack, this.side, dir, simulate)) continue;
            //Check the handler.
            var cap = TesseractCapUtils.getFluidHandler(tile.getLevel(), BlockPos.of(Pos.offset(pos, dir)), dir.getOpposite());
            if (cap.isEmpty()) continue;
            //Perform insertion, and add to the transaction.
            var handler = cap.get();
            long amount = handler.insertFluid(stack,  simulate);
            if (amount > 0) {
                stack.setAmount(stack.getFluidAmount() - amount);
                transaction.setData(transaction.getData() + amount);
            }
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
