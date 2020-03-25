package tesseract;

import net.minecraftforge.fml.common.Mod;
import tesseract.electric.ElectricNet;

@Mod(Constants.API_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {

	public Tesseract() {
		new ElectricNet();
	}
}
