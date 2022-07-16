package tesseract.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.fabric.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;

@Mixin(TesseractGTCapability.class)
public class TesseractGTCapabilityMixin implements IEnergyHandlerStorage {
    @Override
    public IEnergyHandler getEnergyHandler() {
        return (TesseractGTCapability)(Object)this;
    }
}
