package tesseract.api.forge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tesseract.Tesseract;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.heat.IHeatHandler;

@Mod.EventBusSubscriber(modid = Tesseract.API_ID)
public class TesseractCaps {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IHeatHandler> HEAT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return ENERGY_HANDLER_CAPABILITY;
    }
    public static Capability<IHeatHandler> getHEAT_CAPABILITY(){
        return HEAT_CAPABILITY;
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
        ev.register(IHeatHandler.class);
    }
}
