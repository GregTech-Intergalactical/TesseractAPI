package tesseract.api.forge.wrapper;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.FluidSnapshot;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHolder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import tesseract.api.fluid.IFluidNode;

import java.util.ArrayList;
import java.util.List;

public record FluidTileWrapper(BlockEntity tile,
                               IFluidHandler handler) implements IFluidNode {

    @Override
    public int getPriority(Direction direction) {
        return (!(handler instanceof IFluidNode) ? 0 : ((IFluidNode) handler).getPriority(direction));
    }

    @Override
    public boolean allowsExtraction() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).allowsExtraction());
    }

    @Override
    public FluidSnapshot createSnapshot() {
        return null;
    }

    @Override
    public boolean allowsInsertion() {
        return (!(handler instanceof IFluidNode) || ((IFluidNode) handler).allowsInsertion());
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
    public int getSize() {
        return handler.getTanks();
    }

    @Override
    public boolean isEmpty() {
        return getFluids().isEmpty() || getFluids().stream().allMatch(FluidHolder::isEmpty);
    }

    @Override
    public FluidContainer copy() {
        return null;
    }

    @NotNull
    @Override
    public FluidHolder getFluidInTank(int tank) {
        return new ForgeFluidHolder(handler.getFluidInTank(tank));
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public void fromContainer(FluidContainer container) {

    }

    @Override
    public long extractFromSlot(FluidHolder fluidHolder, FluidHolder toInsert, Runnable snapshot) {
        return 0;
    }

    @Override
    public List<FluidHolder> getFluids() {
        List<FluidHolder> fluids = new ArrayList<>();
        for (int i = 0; i < handler.getTanks(); i++) {
            fluids.add(getFluidInTank(i));
        }
        return fluids;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidHolder stack) {
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

    @Override
    public void setFluid(int slot, FluidHolder fluid) {

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
        getFluids().forEach(f -> this.drainInput(f, false));
    }
}
