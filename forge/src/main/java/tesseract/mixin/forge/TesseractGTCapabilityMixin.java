package tesseract.mixin.forge;

import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;

@Mixin(value = TesseractGTCapability.class, remap = false)
public class TesseractGTCapabilityMixin implements IEnergyHandlerStorage {
    @Override
    public IEnergyHandler getEnergyHandler() {
        return (TesseractGTCapability)(Object)this;
    }
}
