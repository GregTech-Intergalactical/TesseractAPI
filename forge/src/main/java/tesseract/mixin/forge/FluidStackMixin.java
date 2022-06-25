package tesseract.mixin.forge;

import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Shadow public abstract int getAmount();

    public long getRealAmount(){
        return getAmount();
    }
}
