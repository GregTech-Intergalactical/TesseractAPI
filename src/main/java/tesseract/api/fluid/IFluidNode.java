package tesseract.api.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;


import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import tesseract.api.GraphWrapper;
import tesseract.graph.Graph;

/**
 * An fluid node is the unit of interaction with fluid inventories.
 * <p>
 * This interface represents a Fluid Tank. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Fluids in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 * </p>
 */
public interface IFluidNode extends IFluidHandler {
    /**
     * @param direction Direction to the proceed.
     * @return Returns the priority of this node as a number.
     */
    int getPriority(Direction direction);

    /**
     * Gets if this storage can have fluid extracted.
     *
     * @return If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive fluid.
     *
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput();

    /**
     * Used to determine if this storage can receive fluid.
     *
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput(Direction direction);

    /**
     * Used to determine which sides can output fluid (if any).
     *
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Direction direction);

    /**
     * Used to determine which fluids and at which direction can be consumed.
     *
     * @param fluid     The Fluid to be queried.
     * @param direction Direction to the input.
     * @return If the tank can input the fluid (EVER, not at the time of query).
     */
    boolean canInput(FluidStack fluid, Direction direction);

    /**
     * Drains from the input tanks rather than output tanks. Useful for recipes.
     *
     * @param stack  stack to drain.
     * @param action execute/simulate
     * @return the drained stack
     */
    default FluidStack drainInput(FluidStack stack, IFluidHandler.FluidAction action) {
        return drain(stack, action);
    }

    class FluidTileWrapper implements IFluidNode {
        private final TileEntity tile;
        private final IFluidHandler handler;

        public FluidTileWrapper(TileEntity tile, IFluidHandler handler) {
            this.tile = tile;
            this.handler = handler;
        }

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
        public int getTankCapacity(int tank) {
            return handler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return handler.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return handler.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return handler.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return handler.drain(maxDrain, action);
        }
    }

    GraphWrapper.ICapabilityGetter<IFluidNode> GETTER = ((level, pos, capSide, capCallback) ->  {
        TileEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IFluidHandler> capability = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capSide);
        if (capability.isPresent()) {
            if (capCallback != null) capability.addListener(o -> capCallback.run());
            IFluidHandler handler = capability.orElse(null);
            return handler instanceof IFluidNode ? (IFluidNode) handler: new IFluidNode.FluidTileWrapper(tile, handler);
        } else {
            return null;
        }
    });
}
