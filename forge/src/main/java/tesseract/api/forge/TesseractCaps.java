package tesseract.api.forge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tesseract.api.gt.IEnergyHandler;

public class TesseractCaps {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
    }
}
