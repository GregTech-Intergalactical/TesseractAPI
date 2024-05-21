package tesseract.mixin.fabric;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.impl.energy.FabricBlockEnergyContainer;
import earth.terrarium.botarium.util.Updatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FabricBlockEnergyContainer.class, remap = false)
public interface FabricBlockEnergyContainerAccessor<T extends EnergyContainer & Updatable> {
    @Accessor
    T getContainer();
}
