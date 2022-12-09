package tesseract.api.forge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tesseract.Tesseract;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;

@Mod.EventBusSubscriber(modid = Tesseract.API_ID)
public class TesseractCaps {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IEnergyHandlerItem> ENERGY_HANDLER_CAPABILITY_ITEM = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IHeatHandler> HEAT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
        ev.register(IEnergyHandlerItem.class);
        ev.register(IHeatHandler.class);
    }
}
