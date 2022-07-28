package tesseract.mixin.forge;

import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidStack.class, remap = false)
public abstract class FluidStackMixin {
    @Shadow public abstract int getAmount();

    @Shadow public abstract void setAmount(int par1);

    public long getRealAmount(){
        return getAmount();
    }

    public void setAmount(long amount){
        setAmount((int)amount);
    }
}
