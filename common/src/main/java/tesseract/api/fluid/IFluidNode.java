package tesseract.api.fluid;


import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import net.minecraft.core.Direction;
import tesseract.TesseractCapUtils;
import tesseract.api.GraphWrapper;

/**
 * An fluid node is the unit of interaction with fluid inventories.
 * <p>
 * This interface represents a Fluid Tank. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Fluids in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 * </p>
 */
public interface IFluidNode extends FluidContainer {
    /**
     * @param direction Direction to the proceed.
     * @return Returns the priority of this node as a number.
     */
    int getPriority(Direction direction);

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
    boolean canInput(FluidHolder fluid, Direction direction);

    /**
     * Drains from the input tanks rather than output tanks. Useful for recipes.
     *
     * @param stack  stack to drain.
     * @param simulate execute/simulate
     * @return the drained stack
     */
    default FluidHolder drainInput(FluidHolder stack, boolean simulate) {
        return extractFluid(stack, simulate);
    }

    default FluidHolder extractFluid(long toExtract, boolean simulate) {
        for (int i = 0; i < getSize(); i++) {
            FluidHolder fluid = extractFluid(getFluidInTank(i), simulate);
            if (!fluid.isEmpty()) return fluid;
        }
        return FluidHolder.empty();
    }

    default boolean isFluidValid(int tank, FluidHolder stack) { return true; }

    default FluidHolder getFluidInTank(int tank){
        return getFluids().get(tank);
    }

    GraphWrapper.ICapabilityGetter<IFluidNode> GETTER = (TesseractCapUtils.INSTANCE::getFluidNode);
}
