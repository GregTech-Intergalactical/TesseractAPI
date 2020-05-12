package tesseract;

import net.minecraftforge.fml.common.Mod;
import tesseract.api.GraphWrapper;
import tesseract.api.electric.ElectricController;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;

@Mod(Tesseract.API_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {

	public static final String API_ID = "tesseract";
	public static final String API_NAME = "Tesseract API";

	public static final GraphWrapper<IElectricCable, IElectricNode> ELECTRIC = new GraphWrapper<>(ElectricController::new);
	public static final GraphWrapper<IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new);
	public static final GraphWrapper<IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(FluidController::new);

	public Tesseract() {
	}
}
