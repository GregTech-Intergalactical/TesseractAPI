package tesseract.api.fabric;


import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;

public class TesseractCapsImpl {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(IEnergyHandler.class);

    public static final Capability<IHeatHandler> HEAT_CAPABILITY = CapabilityManager.get(IHeatHandler.class);
    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return ENERGY_HANDLER_CAPABILITY;
    }
    public static Capability<IHeatHandler> getHEAT_CAPABILITY(){
        return HEAT_CAPABILITY;
    }
}
