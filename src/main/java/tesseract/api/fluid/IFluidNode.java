package tesseract.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.api.IConnectable;
import tesseract.api.IRefreshable;
import tesseract.util.Dir;

/**
 * An fluid node is the unit of interaction with fluid inventories.
 * <p>
 * This interface represents a Fluid Tank. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Fluids in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 * </p>
 */
public interface IFluidNode extends IFluidHandler, IRefreshable {
    /**
     * @param direction Direction to the proceed.
     * @return Returns the priority of this node as a number.
     */
    int getPriority(Dir direction);

    /**
     * Gets if this storage can have fluid extracted.
     * @return If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive fluid.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput();

    /**
     * Used to determine if this storage can receive fluid.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput(Dir direction);
    /**
     * Used to determine which sides can output fluid (if any).
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Dir direction);

    /**
     * Used to determine which fluids and at which direction can be consumed.
     * @param fluid The Fluid to be queried.
     * @param direction Direction to the input.
     * @return If the tank can input the fluid (EVER, not at the time of query).
     */
    boolean canInput(FluidStack fluid, Dir direction);
}
