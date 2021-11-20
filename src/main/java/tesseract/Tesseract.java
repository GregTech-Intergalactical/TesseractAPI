package tesseract;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tesseract.api.GraphWrapper;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.api.item.ItemTransaction;
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

    public static final Logger LOGGER = LogManager.getLogger(API_ID);

    private final static Set<IWorld> firstTick = new ObjectOpenHashSet<>();
    //public static GraphWrapper<Integer, IFECable, IFENode> FE_ENERGY = new GraphWrapper<>(FEController::new);
    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new);
    public static GraphWrapper<FluidTransaction, IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(Fluid::new);
    public static GraphWrapper<ItemTransaction, IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new);

    public static final int HEALTH_CHECK_TIME = 1000;

    public Tesseract() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStoppedEvent);
        MinecraftForge.EVENT_BUS.addListener(this::worldUnloadEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    public static boolean hadFirstTick(IWorld world) {
        return firstTick.contains(world);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        TesseractGTCapability.register();
    }

    public void serverStoppedEvent(FMLServerStoppedEvent e) {
        firstTick.clear();
        //FE_ENERGY.clear();
        GT_ENERGY.clear();
        ITEM.clear();
        FLUID.clear();
    }

    public void worldUnloadEvent(WorldEvent.Unload e) {
        if (!(e.getWorld() instanceof World) || ((World) e.getWorld()).isClientSide) return;
        //FE_ENERGY.removeWorld((World) e.getWorld());
        GT_ENERGY.removeWorld((World) e.getWorld());
        ITEM.removeWorld((World) e.getWorld());
        FLUID.removeWorld((World) e.getWorld());
        firstTick.remove(e.getWorld());
    }

    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient()) return;
        World dim = event.world;
        if (!hadFirstTick(dim)) {
            firstTick.add(event.world);
            GT_ENERGY.onFirstTick(dim);
            //FE_ENERGY.onFirstTick(dim);
            FLUID.onFirstTick(dim);
            ITEM.onFirstTick(dim);
        }
        if (event.phase == TickEvent.Phase.START) {
            GT_ENERGY.tick(dim);
            //FE_ENERGY.tick(dim);
            FLUID.tick(dim);
            ITEM.tick(dim);
        }
        if (HEALTH_CHECK_TIME > 0 && event.world.getGameTime() % HEALTH_CHECK_TIME == 0) {
            GT_ENERGY.healthCheck();
            //FE_ENERGY.healthCheck();
            FLUID.healthCheck();
            ITEM.healthCheck();
        }
    }
}
