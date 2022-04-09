package tesseract.fabric;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import tesseract.Tesseract;
import tesseract.api.GraphWrapper;
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

public class TesseractImpl implements ModInitializer {
    private final static Set<LevelAccessor> firstTick = new ObjectOpenHashSet<>();
    //public static GraphWrapper<Integer, IFECable, IFENode> FE_ENERGY = new GraphWrapper<>(FEController::new);
    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new, IGTNode.GT_GETTER);
    public static GraphWrapper<FluidTransaction, IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(Fluid::new, IFluidNode.GETTER);
    public static GraphWrapper<ItemTransaction, IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new, IItemNode.GETTER);

    public TesseractImpl(){
    }

    public static boolean hadFirstTick(LevelAccessor world) {
        return firstTick.contains(world);
    }

    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> getGT_ENERGY(){
        return GT_ENERGY;
    }

    public static GraphWrapper<FluidTransaction, IFluidPipe, IFluidNode> getFLUID(){
        return FLUID;
    }

    public static GraphWrapper<ItemTransaction, IItemPipe, IItemNode> getITEM(){
        return ITEM;
    }

    private static void onWorldUnload(MinecraftServer server, ServerLevel world) {
        if (world == null) return;
        //FE_ENERGY.removeWorld((World) e.getWorld());
        GraphWrapper.getWrappers().forEach(g -> g.removeWorld(world));
        firstTick.remove(world);
    }

    private static void onEndTick(ServerLevel l) {
        if (!hadFirstTick(l)) {
            firstTick.add(l);
            GraphWrapper.getWrappers().forEach(t -> t.onFirstTick(l));
        }
        if (Tesseract.HEALTH_CHECK_TIME > 0 && l.getGameTime() % Tesseract.HEALTH_CHECK_TIME == 0) {
            GraphWrapper.getWrappers().forEach(GraphWrapper::healthCheck);
        }
    }

    private static void onStartTick(ServerLevel l) {
        if (!hadFirstTick(l)) {
            firstTick.add(l);
            GraphWrapper.getWrappers().forEach(t -> t.onFirstTick(l));
        }
        GraphWrapper.getWrappers().forEach(t -> t.tick(l));
        if (Tesseract.HEALTH_CHECK_TIME > 0 && l.getGameTime() % Tesseract.HEALTH_CHECK_TIME == 0) {
            GraphWrapper.getWrappers().forEach(GraphWrapper::healthCheck);
        }
    }

    private static void onServerStopping(MinecraftServer s) {
        firstTick.clear();
        //FE_ENERGY.clear();
        GraphWrapper.getWrappers().forEach(GraphWrapper::clear);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(TesseractImpl::onServerStopping);
        ServerTickEvents.START_WORLD_TICK.register(TesseractImpl::onStartTick);
        ServerTickEvents.END_WORLD_TICK.register(TesseractImpl::onEndTick);
        ServerWorldEvents.UNLOAD.register((TesseractImpl::onWorldUnload));
    }
}
