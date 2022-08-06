package tesseract.mixin.fabric.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.blockentities.AbstractStorageMachineBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tesseract.api.fabric.wrapper.IEnergyMoveableTiers;

@Mixin(AbstractStorageMachineBlockEntity.class)
public class AbstractStorageMachineBEMixin implements IEnergyMoveableTiers {
    @Shadow @Final protected CableTier to;

    @Shadow @Final protected CableTier from;

    @Override
    public long getOutputVoltage() {
        return to.getEu();
    }

    @Override
    public long getInputVoltage() {
        return from.getEu();
    }
}
