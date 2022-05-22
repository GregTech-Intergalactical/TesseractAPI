package net.minecraftforge.fluids.capability;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidHandler {
    enum FluidAction {
        EXECUTE, SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }
    int getTanks();
    FluidStack getFluidInTank(int tank);
    long getTankCapacity(int tank);
    long fill(FluidStack stack, FluidAction action); // returns amount filled
    FluidStack drain(FluidStack stack, FluidAction action); // returns amount drained
    FluidStack drain(long amount, FluidAction action); // returns amount drained
    default FluidStack drain(int amount, FluidAction action){
        return drain((long)amount, action);
    }
    default boolean isFluidValid(int tank, FluidStack stack) { return true; }
}
