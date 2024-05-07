package tesseract.api.fluid;




import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;

import java.util.List;

public interface FluidContainerHandler extends PlatformFluidHandler {
    FluidContainer getFluidContainer();
    @Override
    default long insertFluid(FluidHolder fluid, boolean simulate) {
        return getFluidContainer().insertFluid(fluid, simulate);
    }

    @Override
    default FluidHolder extractFluid(FluidHolder fluid, boolean simulate) {
        return getFluidContainer().extractFluid(fluid, simulate);
    }

    @Override
    default int getTankAmount() {
        return getFluidContainer().getSize();
    }

    @Override
    default FluidHolder getFluidInTank(int tank) {
        return getFluidContainer().getFluids().get(tank);
    }

    @Override
    default List<FluidHolder> getFluidTanks() {
        return getFluidContainer().getFluids();
    }

    @Override
    default long getTankCapacity(int tank) {
        return getFluidContainer().getTankCapacity(tank);
    }

    @Override
    default boolean supportsInsertion() {
        return getFluidContainer().allowsInsertion();
    }

    @Override
    default boolean supportsExtraction() {
        return getFluidContainer().allowsExtraction();
    }
}
