package tesseract.api;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.heat.IHeatHandler;

public class TesseractCaps {
    @ExpectPlatform
    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return null;
    }

    @ExpectPlatform
    public static Capability<IHeatHandler> getHEAT_CAPABILITY(){
        return null;
    }

    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
        ev.register(IHeatHandler.class);
    }
}
