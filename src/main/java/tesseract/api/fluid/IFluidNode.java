package tesseract.api.fluid;

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
public interface IFluidNode<T> extends IConnectable, IRefreshable {

    /**
     * Adds fluid to the node. Returns amount of fluid that was filled.
     * @param data FluidData attempting to fill the tank.
     * @param simulate If true, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    int insert(FluidData<T> data, boolean simulate);

    /**
     * Removes fluid from the node. Returns amount of fluid that was drained.
     * @param tank The tank to extract from.
     * @param amount Maximum amount of fluid to be removed from the container.
     * @param simulate If true, the drain will only be simulated.
     * @return FluidData representing fluid that was removed (or would be, if simulated) from the tank.
     */
    FluidData<T> extract(int tank, int amount, boolean simulate);

    /**
     * @param direction Direction to the proceed.
     * @return Gets any available tank. (-1 when wasn't found any)
     **/
    int getAvailableTank(Dir direction);

    /**
     * @param direction Direction to the proceed.
     * @return Gets the initial amount of pressure that can be output.
     */
    int getOutputAmount(Dir direction);

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
    boolean canInput(Object fluid, Dir direction);
}
