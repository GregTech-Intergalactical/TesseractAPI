package tesseract.mixin.fabric.mi;

import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.fabric.wrapper.IEnergyHandlerMoveable;
import tesseract.api.gt.IEnergyHandler;

@Mixin(IEnergyHandler.class)
public interface IEnergyHandlerMixin extends IEnergyHandlerMoveable {
}
