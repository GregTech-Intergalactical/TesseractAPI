package tesseract.mixin.fabric;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.fabric.energy.FabricBlockEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FabricBlockEnergyContainer.class, remap = false)
public interface FabricBlockEnergyContainerAccessor {
    @Accessor
    EnergyContainer getContainer();
}
