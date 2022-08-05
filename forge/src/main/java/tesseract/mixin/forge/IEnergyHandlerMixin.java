package tesseract.mixin.forge;

import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.forge.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;

@Mixin(value = IEnergyHandler.class, remap = false)
public interface IEnergyHandlerMixin extends IEnergyHandlerStorage {
    @Override
    default IEnergyHandler getEnergyHandler(){
        return (IEnergyHandler) this;
    }
}
