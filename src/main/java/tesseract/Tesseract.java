package tesseract;

import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import tesseract.api.GraphWrapper;
import tesseract.api.fe.FEController;
import tesseract.api.fe.IFECable;
import tesseract.api.fe.IFENode;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.controller.Energy;
import tesseract.controller.Fluid;

@Mod(Tesseract.API_ID)
//@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {

	public static final String API_ID = "tesseract";
	public static final String API_NAME = "Tesseract API";
	public static final String VERSION = "0.0.1";
	public static final String DEPENDS = "";

	public static GraphWrapper<IFECable, IFENode> FE_ENERGY;
	public static GraphWrapper<IGTCable, IGTNode> GT_ENERGY;
	public static GraphWrapper<IFluidPipe, IFluidNode<FluidStack>> FLUID;
	public static GraphWrapper<IItemPipe, IItemNode> ITEM;

	public Tesseract() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void init(FMLServerAboutToStartEvent e) {
		FE_ENERGY = new GraphWrapper<>(e.getServer()::getWorld,FEController::new);
		GT_ENERGY = new GraphWrapper<>(e.getServer()::getWorld,Energy::new);
		FLUID = new GraphWrapper<>(e.getServer()::getWorld,Fluid::new);
		ITEM = new GraphWrapper<>(e.getServer()::getWorld,ItemController::new);
	}
    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
		if (event.side.isServer() && event.phase == TickEvent.Phase.START) {
			RegistryKey<World> dim = event.world.getDimensionKey();
            GT_ENERGY.tick(dim);
            FE_ENERGY.tick(dim);
            FLUID.tick(dim);
            ITEM.tick(dim);
        }
    }
}
