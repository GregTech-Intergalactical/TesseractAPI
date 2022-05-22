package tesseract.api;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraftforge.common.capabilities.Capability;
import tesseract.api.gt.IEnergyHandler;

public class TesseractCaps {
    @ExpectPlatform
    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return null;
    }
}
