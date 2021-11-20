package tesseract.api.capability;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.Tesseract;
import tesseract.api.fluid.FluidTransaction;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nonnull;

public class TesseractFluidCapability implements IFluidHandler {
    public final TileEntity tile;
    public final Direction side;

    private FluidTransaction old;

    public TesseractFluidCapability(TileEntity tile, Direction dir) {
        this.tile = tile;
        this.side = dir;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (action.execute()) {
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            FluidTransaction transaction = new FluidTransaction(resource.copy(), a -> {
            });
            Tesseract.FLUID.getController(tile.getLevel(), pos).insert(side == null ? pos : Pos.offset(pos, Graph.DIRECTIONS[side.get3DDataValue()]), pos, transaction);
            this.old = transaction;
        }
        return resource.getAmount() - this.old.stack.getAmount();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
