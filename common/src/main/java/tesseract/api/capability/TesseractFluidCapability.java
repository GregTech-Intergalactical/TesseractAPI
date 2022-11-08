package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractGraphWrappers;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
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
    public long getTankCapacityInDroplets(int tank) {
        return Integer.MAX_VALUE * TesseractGraphWrappers.dropletMultiplier;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public long fillDroplets(FluidStack resource, FluidAction action) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (action.execute()) {
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            FluidTransaction transaction = new FluidTransaction(resource.copy(), a -> {});
            if (!this.isNode) {
                TesseractGraphWrappers.FLUID.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        this.isSending = false;
        return resource.getRealAmount() - this.old.stack.getRealAmount();
    }


    private void transferAroundPipe(FluidTransaction transaction, long pos) {
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                FluidStack stack = transaction.stack.copy();
                this.callback.modify(stack, this.side, dir, true);
                //Check the handler.
                var cap = otherTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                if (!cap.isPresent()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.resolve().get();
                long amount = handler.fillDroplets(stack,  IFluidHandler.FluidAction.SIMULATE);
                if (amount > 0) {
                    stack.setAmount(amount);
                    transaction.addData(stack, a -> {
                        this.callback.modify(a, this.side, dir, false);
                        handler.fillDroplets(a, FluidAction.EXECUTE);
                    });
                }
                if (transaction.stack.isEmpty()) break;
            }
        }
    }

    public int fill(FluidStack resource, FluidAction action){
        return (int) (fillDroplets(resource, action) / TesseractGraphWrappers.dropletMultiplier);
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action){
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
