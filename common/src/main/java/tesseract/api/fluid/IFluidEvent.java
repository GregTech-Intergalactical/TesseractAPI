package tesseract.api.fluid;

import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Interface for handling a fluid events. (Controller will handle them)
 */
public interface IFluidEvent<T> {

    /**
     * Executes when the cable trying to transport higher amount of pressure than can.
     *
     * @param world    The world.
     * @param pos      The pipe position.
     * @param pressure The current pressure.
     */
    default void onPipeOverPressure(Level world, long pos, long pressure, FluidStack fluid) {
        //NOOP
    }

    /**
     * Executes when the cable trying to transport higher amount of liquids than can.
     *
     * @param world    The world.
     * @param pos      The pipe position.
     * @param capacity The current capacity.
     */
    default void onPipeOverCapacity(Level world, long pos, long capacity, FluidStack fluid) {
        //NOOP
    }

    /**
     * Executes when the cable trying to transport higher amount of temperature than can.
     *
     * @param world       The world.
     * @param pos         The pipe position.
     * @param temperature The current temperature.
     */
    default void onPipeOverTemp(Level world, long pos, int temperature) {
        //NOOP
    }

    /**
     * Executes when the pipe trying to transport gas that can leak.
     * Returns resulting fluid stack
     *
     * @param world The world.
     * @param pos   The pipe position.
     * @param fluid FluidData holding the Fluid to be queried.
     */
    default FluidStack onPipeGasLeak(Level world, long pos, FluidStack fluid) {
        return fluid;
    }
}
