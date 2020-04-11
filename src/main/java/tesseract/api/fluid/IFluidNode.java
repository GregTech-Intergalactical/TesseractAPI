package tesseract.api.fluid;

import tesseract.api.IConnectable;
import tesseract.graph.ITickHost;
import tesseract.util.Dir;

import javax.annotation.Nonnull;

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
     * @param maxReceive Maximum amount of fluid to be inserted.
     * @param simulate If true, the insertion will only be simulated.
     * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
     */
    int insert(long maxReceive, boolean simulate);

    /**
     * Removes fluid from the node. Returns amount of fluid that was drained.
     * @param maxExtract Maximum amount of fluid to be removed from the container.
     * @param simulate If true, the extraction will only be simulated.
     * @return Amount of fluid that was removed (or would be, if simulated) from the tank.
     */
    long extract(long maxExtract, boolean simulate);

    /**
     * @return Id representing the fluid in the tank, -1 if the tank is empty.
     */
    int getFluidId();

    /**
     * @return Current amount of fluid in the tank.
     */
    long getFluidAmount();

    /**
     * @return Capacity of this fluid tank.
     */
    long getCapacity();

    /**
     * @return Gets the initial amount of pressure that can be output.
     */
    long getOutputPressure();

    /**
     * @return Gets the maximum amount of pressure that can be input.
     */
    long getInputPressure();

    /**
     * Gets if this storage can have energy extracted.
     * @return If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive energy.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput();

    /**
     * Used to determine which sides can output energy (if any).
     * Output cannot be used as input.
     * @param direction Direction to test.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(@Nonnull Dir direction);
}
