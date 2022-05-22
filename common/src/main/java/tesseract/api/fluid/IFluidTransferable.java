package tesseract.api.fluid;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IFluidTransferable {
    LazyOptional<IFluidHandler> getFluidHandler(Direction side);
}
