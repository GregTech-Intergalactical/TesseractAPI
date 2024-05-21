package tesseract.mixin.fabric;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.impl.fluid.storage.FabricBlockFluidContainer;
import earth.terrarium.botarium.util.Updatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FabricBlockFluidContainer.class)
public interface FabricBlockFluidContainerAccessor<T extends FluidContainer & Updatable> {

    @Accessor
    T getContainer();
}
