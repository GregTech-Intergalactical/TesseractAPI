package tesseract.mixin.forge;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;

@Mixin(IFluidHandler.class)
public interface IFluidHandlerMixin {
    @Shadow
    int getTankCapacity(int tank);
    @Shadow
    int fill(FluidStack stack, IFluidHandler.FluidAction action);
    @Shadow
    FluidStack drain(int i, IFluidHandler.FluidAction fluidAction);

    default long getTankCapacityLong(int tank){
        return getTankCapacity(tank);
    }
    default long fillLong(FluidStack stack, IFluidHandler.FluidAction action){
        return fill(stack, action);
    }

    default FluidStack drain(long i, IFluidHandler.FluidAction fluidAction){
        return drain((int)i, fluidAction);
    }
}
