package tesseract.api.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;

public record FluidTileWrapper(BlockEntity tile,
                               IFluidHandler handler) implements IFluidNode {

    @Override
    public int getPriority(Direction direction) {
        return (!(handler instanceof IFluidNode) ? 0 : ((IFluidNode) handler).getPriority(direction));
    }

    @Override
    public boolean canOutput() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canOutput());
    }

    @Override
    public boolean canInput() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canInput());
    }

    @Override
    public boolean canInput(Direction direction) {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canInput(direction));
    }

    @Override
    public boolean canOutput(Direction direction) {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canOutput(direction));
    }

    @Override
    public boolean canInput(FluidStack fluid, Direction direction) {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canInput(fluid, direction));
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return handler.getFluidInTank(tank);
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return handler.isFluidValid(tank, stack);
    }

    @Override
    public long fill(FluidStack resource, FluidAction action) {
        return handler.fill(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return handler.drain(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(long maxDrain, FluidAction action) {
        return handler.drain(maxDrain, action);
    }
}
