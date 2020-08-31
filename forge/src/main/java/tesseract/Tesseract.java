package tesseract;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import tesseract.api.GraphWrapper;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.fe.FEController;
import tesseract.api.fe.IFECable;
import tesseract.api.fe.IFENode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.controller.Energy;
import tesseract.controller.Fluid;

@Mod(Tesseract.API_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {

	public static final String API_ID = "tesseract";
	public static final String API_NAME = "Tesseract API";

	public static final GraphWrapper<IFECable, IFENode> FE_ENERGY = new GraphWrapper<>(FEController::new);
	public static final GraphWrapper<IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new);
	public static final GraphWrapper<IFluidPipe, IFluidNode<FluidStack>> FLUID = new GraphWrapper<>(Fluid::new);
	public static final GraphWrapper<IItemPipe, IItemNode<ItemStack>> ITEM = new GraphWrapper<>(ItemController::new);
}
