package tesseract;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import tesseract.api.GraphWrapper;
import tesseract.api.energygt.IElectricCable;
import tesseract.api.energygt.IElectricNode;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.energy.EnergyController;
import tesseract.api.energy.ITesseractCable;
import tesseract.api.energy.ITesseractNode;
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
	public static final GraphWrapper<ITesseractCable, ITesseractNode> FLUX = new GraphWrapper<>(EnergyController::new);
	public static final GraphWrapper<IItemPipe, IItemNode<ItemStack>> ITEM = new GraphWrapper<>(ItemController::new);
	public static final GraphWrapper<IFluidPipe, IFluidNode<FluidStack>> FLUID = new GraphWrapper<>(Fluid::new);
}
