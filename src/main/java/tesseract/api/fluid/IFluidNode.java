package tesseract.api.fluid;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.graph.IConnectable;
import tesseract.graph.ITickHost;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An fluid node is the unit of interaction with fluid inventories.
 * <p>
 * A reference implementation can be found at {@link net.minecraftforge.fluids.IFluidTank}.
 *
 * This interface represents a Fluid Tank. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Fluids in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 */
public interface IFluidNode extends IConnectable, ITickHost {

    /**
     * Adds fluid to the node. Returns amount of fluid that was filled.
     * @param data FluidData attempting to fill the tank.
     * @param simulate If true, the fill will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    int insert(@Nonnull FluidData data, boolean simulate);

    /**
     * Removes fluid from the node. Returns amount of fluid that was drained.
     * @param tank The tank to extract from.
     * @param amount Maximum amount of fluid to be removed from the container.
     * @param simulate If true, the drain will only be simulated.
     * @return FluidData representing fluid that was removed (or would be, if simulated) from the tank.
     */
    @Nullable
    FluidData extract(@Nonnull Object tank, int amount, boolean simulate);

    /**
     * @return Gets any available tank.
     **/
    @Nullable
    Object getAvailableTank();

    /**
     * @param fluid The Fluid to be queried.
     * @return If the tank can hold the fluid (EVER, not at the time of query).
     */
    boolean canHold(@Nonnull Object fluid);

    /**
     * @return Gets the maximum amount of fluid that can be stored.
     */
    int getCapacity();

    /**
     * @return Gets the initial amount of pressure that can be output.
     */
    int getOutputPressure();

    /**
     * @return Gets the maximum amount of pressure that can be input.
     */
    int getInputPressure();

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
     * Output cannot be used as input.
     * @param direction Direction to the out.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(@Nonnull Dir direction);

    /**
     * Used to determine which fluids can be output (if any).
     * @param direction Direction to the out.
     * @return Gets the array of the filtered fluids.
     */
    @Nonnull
    ObjectSet<?> getOutputFilter(@Nonnull Dir direction);

    /**
     * Used to determine which fluids can be input (if any).
     * @param direction Direction to the out.
     * @return Gets the array of the filtered fluids.
     */
    @Nonnull
    ObjectSet<?> getInputFilter(@Nonnull Dir direction);
}
