package tesseract.api.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.TesseractPlatformUtils;
import tesseract.api.GraphWrapper;
import tesseract.api.wrapper.FluidTileWrapper;

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

    /**
     * Drains from the input tanks rather than output tanks. Useful for recipes.
     *
     * @param stack  stack to drain.
     * @param sim execute/simulate
     * @return the drained stack
     */
    default FluidStack drainInput(FluidStack stack, boolean sim) {
        return drain(stack, sim ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    GraphWrapper.ICapabilityGetter<IFluidNode> GETTER = (IFluidNode::getFluidNode);

    static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IFluidHandler> capability = TesseractPlatformUtils.getFluidHandler(tile, capSide);
        if (capability.isPresent()) {
            if (capCallback != null) capability.addListener(o -> capCallback.run());
            IFluidHandler handler = capability.orElse(null);
            return handler instanceof IFluidNode ? (IFluidNode) handler: new FluidTileWrapper(tile, handler);
        } else {
            return null;
        }
    }
}
