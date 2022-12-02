package tesseract.fabric;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyMoveable;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.config.ModConfig;
import team.reborn.energy.api.EnergyStorage;
import tesseract.Tesseract;
import tesseract.TesseractConfig;
import tesseract.api.GraphWrapper;
import tesseract.api.TesseractCaps;
import tesseract.api.fabric.TesseractCapsImpl;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.api.item.ItemTransaction;
import tesseract.controller.Energy;
import tesseract.controller.Fluid;

import java.util.Set;
import java.util.function.BiFunction;

public class TesseractImpl extends Tesseract implements ModInitializer {
    //public static GraphWrapper<Integer, IFECable, IFENode> FE_ENERGY = new GraphWrapper<>(FEController::new);
    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new, IGTNode.GT_GETTER);

    public TesseractImpl(){
    }

    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> getGT_ENERGY(){
        return GT_ENERGY;
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
        Tesseract.init();
        ModLoadingContext.registerConfig(Tesseract.API_ID, ModConfig.Type.COMMON, TesseractConfig.COMMON_SPEC);
        RegisterCapabilitiesEvent.REGISTER_CAPS.register(TesseractCaps::register);
        ServerLifecycleEvents.SERVER_STOPPING.register(TesseractImpl::onServerStopping);
        ServerTickEvents.START_WORLD_TICK.register(TesseractImpl::onStartTick);
        ServerTickEvents.END_WORLD_TICK.register(TesseractImpl::onEndTick);
        ServerWorldEvents.UNLOAD.register((TesseractImpl::onWorldUnload));
        ModConfigEvent.LOADING.register(TesseractConfig::onModConfigEvent);
        ModConfigEvent.RELOADING.register(TesseractConfig::onModConfigEvent);
    }

    public static <T extends BlockEntity> void registerMITile(BiFunction<T, Direction, IEnergyHandler> function, BlockEntityType<T> type){
        EnergyApi.MOVEABLE.registerForBlockEntity((blockEntity, direction) -> (EnergyMoveable) function.apply(blockEntity, direction), type);
    }

    public static <T extends BlockEntity> void registerTRETile(BiFunction<T, Direction, IEnergyHandler> function, BlockEntityType<T> type){
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> (EnergyStorage) function.apply(blockEntity, direction), type);
    }
}
