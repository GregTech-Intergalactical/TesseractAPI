package tesseract.api.forge.wrapper;

import earth.terrarium.botarium.api.fluid.FluidHolder;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record FluidTileWrapper(BlockEntity tile,
                               IFluidHandler handler) implements IFluidNode {

    @Override
    public int getPriority(Direction direction) {
        return (!(handler instanceof IFluidNode) ? 0 : ((IFluidNode) handler).getPriority(direction));
    }

    @Override
    public boolean supportsExtraction() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).supportsExtraction());
    }

    @Override
    public boolean supportsInsertion() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).supportsInsertion());
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
    public boolean canInput(FluidHolder fluid, Direction direction) {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).canInput(fluid, direction));
    }

    @Override
    public int getTankAmount() {
        return handler.getTanks();
    }

    @Nonnull
    @Override
    public FluidHolder getFluidInTank(int tank) {
        return new ForgeFluidHolder(handler.getFluidInTank(tank));
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public List<FluidHolder> getFluidTanks() {
        List<FluidHolder> fluids = new ArrayList<>();
        for (int i = 0; i < handler.getTanks(); i++) {
            fluids.add(getFluidInTank(i));
        }
        return fluids;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidHolder stack) {
        return handler.isFluidValid(tank, new ForgeFluidHolder(stack).getFluidStack());
    }

    @Override
    public long insertFluid(FluidHolder fluid, boolean simulate) {
        return handler.fill(new ForgeFluidHolder(fluid).getFluidStack(), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public FluidHolder extractFluid(FluidHolder fluid, boolean simulate) {
        return new ForgeFluidHolder(handler.drain(new ForgeFluidHolder(fluid).getFluidStack(), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE));
    }
}
