package tesseract;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
	public static final String VERSION = "0.0.1";
	public static final String DEPENDS = "";

	public static GraphWrapper<IFECable, IFENode> FE_ENERGY;
	public static GraphWrapper<IGTCable, IGTNode> GT_ENERGY;
	public static GraphWrapper<IFluidPipe, IFluidNode<FluidStack>> FLUID;
	public static GraphWrapper<IItemPipe, IItemNode<ItemStack>> ITEM;

	public Tesseract() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void init(FMLServerAboutToStartEvent e) {
		FE_ENERGY = new GraphWrapper<>(FEController::new);
		GT_ENERGY = new GraphWrapper<>(Energy::new);
		FLUID = new GraphWrapper<>(Fluid::new);
		ITEM = new GraphWrapper<>(ItemController::new);
	}
    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
		if (event.side.isServer()) {
            int dim = event.world.getDimension().getType().getId();
            GT_ENERGY.tick(dim);
            FE_ENERGY.tick(dim);
            FLUID.tick(dim);
            ITEM.tick(dim);
        }
    }
}
