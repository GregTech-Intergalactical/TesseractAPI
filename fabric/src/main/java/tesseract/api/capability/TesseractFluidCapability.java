package tesseract.api.capability;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import tesseract.api.fluid.*;
import tesseract.fabric.TesseractImpl;
import tesseract.graph.Graph;
import tesseract.util.Pos;


public class TesseractFluidCapability<T extends BlockEntity & IFluidPipe> extends TesseractBaseCapability<T> implements IFluidNode {

    private FluidTransaction old;

    public TesseractFluidCapability(T tile, Direction dir, boolean isNode, ITransactionModifier callback) {
        super(tile, dir, isNode, callback);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public long getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public long fill(FluidStack resource, boolean action) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (action) {
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            FluidTransaction transaction = new FluidTransaction(resource.copy(), a -> {
            });
            if (!this.isNode) {
                TesseractImpl.FLUID.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                for (Direction dir : Graph.DIRECTIONS) {
                    if (dir == side || !this.tile.connects(dir)) continue;
                    TesseractImpl.FLUID.getController(tile.getLevel(), pos).insert(Pos.offset(pos, dir), dir.getOpposite(), transaction, callback);
                }
            }
            this.old = transaction;
        }
        this.isSending = false;
        return resource.getAmount() - this.old.stack.getAmount();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, boolean action) {
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean action) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getPriority(Direction direction) {
        return 0;
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
    public boolean canInput(FluidStack fluid, Direction direction) {
        return true;
    }
}
