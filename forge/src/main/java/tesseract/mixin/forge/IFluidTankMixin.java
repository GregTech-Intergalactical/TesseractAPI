package tesseract.mixin.forge;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IFluidTank.class)
public interface IFluidTankMixin {
    default long getFluidAmountLong(){
        return getFluidAmount();
    }

    @Shadow
    int getFluidAmount();

    /**
     * @return Capacity of this fluid tank.
     */
    default long getCapacityLong(){
        return getCapacity();
    }

    @Shadow
    int getCapacity();
    @Shadow
    int fill(FluidStack stack, IFluidHandler.FluidAction action);
    @Shadow
    FluidStack drain(int i, IFluidHandler.FluidAction fluidAction);
    default long fillLong(FluidStack stack, IFluidHandler.FluidAction action){
        return fill(stack, action);
    }

    default FluidStack drain(long i, IFluidHandler.FluidAction fluidAction){
        return drain((int)i, fluidAction);
    }
}
