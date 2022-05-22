package tesseract.api.forge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tesseract.api.gt.IEnergyHandler;

public class TesseractCapsImpl {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return ENERGY_HANDLER_CAPABILITY;
    }
    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
    }
}
