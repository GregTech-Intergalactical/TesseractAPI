package tesseract.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import tesseract.FluidPlatformUtils;

public class TesseractPreLoad implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        FluidPlatformUtils.INSTANCE = new FluidPlatformUtilsImpl();
    }
}
