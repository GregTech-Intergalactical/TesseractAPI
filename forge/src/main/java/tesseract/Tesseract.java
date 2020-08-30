package tesseract;

import net.minecraftforge.fml.common.Mod;
import tesseract.api.GraphWrapper;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.flux.FluxController;
import tesseract.api.flux.IFluxCable;
import tesseract.api.flux.IFluxNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.controller.Electric;
import tesseract.controller.Fluid;

@Mod(Tesseract.API_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {
	public static final String API_ID = "tesseract";
	public static final String API_NAME = "Tesseract API";

	public static final GraphWrapper<IElectricCable, IElectricNode> ELECTRIC = new GraphWrapper<>(Electric::new);
	public static final GraphWrapper<IFluxCable, IFluxNode> FLUX = new GraphWrapper<>(FluxController::new);
	public static final GraphWrapper<IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new);
	public static final GraphWrapper<IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(Fluid::new);
}
