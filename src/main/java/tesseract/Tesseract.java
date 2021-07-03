package tesseract;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tesseract.api.GraphWrapper;
import tesseract.api.capability.TesseractGTCapability;
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

import java.util.Set;

@Mod(Tesseract.API_ID)
//@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Tesseract {

	public static final String API_ID = "tesseract";
	public static final String API_NAME = "Tesseract API";
	public static final String VERSION = "0.0.1";
	public static final String DEPENDS = "";

	public static GraphWrapper<Integer,IFECable, IFENode> FE_ENERGY = new GraphWrapper<>(FEController::new);
	public static GraphWrapper<Long,IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new);
	public static GraphWrapper<FluidStack,IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(Fluid::new);
	public static GraphWrapper<ItemStack,IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new);

	private final static Set<IWorld> firstTick = new ObjectOpenHashSet<>();

	public Tesseract() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.addListener(this::serverStoppedEvent);
		MinecraftForge.EVENT_BUS.addListener(this::worldUnloadEvent);
		MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
	}

	public void commonSetup(FMLCommonSetupEvent event) {
		TesseractGTCapability.register();
	}

	public void serverStoppedEvent(FMLServerStoppedEvent e) {
		firstTick.clear();
		FE_ENERGY.clear();
		GT_ENERGY.clear();
		ITEM.clear();
		FLUID.clear();
	}

	public void worldUnloadEvent(WorldEvent.Unload e) {
		if (!(e.getWorld() instanceof World) || ((World) e.getWorld()).isRemote) return;
		FE_ENERGY.removeWorld((World)e.getWorld());
		GT_ENERGY.removeWorld((World)e.getWorld());
		ITEM.removeWorld((World)e.getWorld());
		FLUID.removeWorld((World)e.getWorld());
		firstTick.remove(e.getWorld());
	}

    public void onServerTick(TickEvent.WorldTickEvent event) {
		if (event.side.isClient()) return;
		World dim = event.world;
		if (!hadFirstTick(dim)) {
			firstTick.add(event.world);
			GT_ENERGY.onFirstTick(dim);
			FE_ENERGY.onFirstTick(dim);
			FLUID.onFirstTick(dim);
			ITEM.onFirstTick(dim);
		}
		if (event.phase == TickEvent.Phase.START) {
            GT_ENERGY.tick(dim);
            FE_ENERGY.tick(dim);
            FLUID.tick(dim);
            ITEM.tick(dim);
        }
    }

	public static boolean hadFirstTick(IWorld world) {
		return firstTick.contains(world);
	}
}
