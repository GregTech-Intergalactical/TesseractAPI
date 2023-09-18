package tesseract.api.fluid;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import tesseract.api.DataHolder;

public class FluidDataHolder extends DataHolder<FluidHolder, Long> {
    public FluidDataHolder(FluidHolder immutableData, Long data) {
        super(immutableData, data);
    }
}
