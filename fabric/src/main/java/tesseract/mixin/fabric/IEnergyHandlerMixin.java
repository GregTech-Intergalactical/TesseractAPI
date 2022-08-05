package tesseract.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.fabric.wrapper.IEnergyHandlerStorage;
import tesseract.api.gt.IEnergyHandler;

@Mixin(IEnergyHandler.class)
public interface IEnergyHandlerMixin extends IEnergyHandlerStorage {
    @Override
    default IEnergyHandler getEnergyHandler(){
        return (IEnergyHandler) this;
    }
}
