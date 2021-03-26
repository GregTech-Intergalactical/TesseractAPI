package tesseract.api.capability;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.Tesseract;
import tesseract.api.ITickingController;
import tesseract.api.fluid.FluidController;
import tesseract.api.item.ItemController;
import tesseract.util.Dir;
import tesseract.util.Pos;

import javax.annotation.Nonnull;

public class TesseractFluidCapability implements IFluidHandler {
    public final TileEntity tile;
    public final Direction side;

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
        ITickingController controller =  Tesseract.FLUID.getController(tile.getWorld().getDimensionKey(), tile.getPos().toLong());
        if (controller == null) return 0;
        return ((FluidController) controller).insert(new Pos(tile.getPos().toLong()), Dir.VALUES[side.getIndex()],resource, action.simulate());
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
