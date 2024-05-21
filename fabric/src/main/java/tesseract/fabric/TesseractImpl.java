package tesseract.fabric;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.impl.energy.FabricBlockEnergyContainer;
import earth.terrarium.botarium.util.Updatable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;
import tesseract.Tesseract;
import tesseract.TesseractConfig;
import tesseract.api.GraphWrapper;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.fabric.TesseractLookups;
import tesseract.api.fabric.wrapper.ContainerItemContextWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyItem;

import java.util.function.BiFunction;

public class TesseractImpl extends Tesseract implements ModInitializer {

    public TesseractImpl(){
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
        ServerLifecycleEvents.SERVER_STOPPING.register(TesseractImpl::onServerStopping);
        ServerTickEvents.START_WORLD_TICK.register(TesseractImpl::onStartTick);
        ServerTickEvents.END_WORLD_TICK.register(TesseractImpl::onEndTick);
        ServerWorldEvents.UNLOAD.register((TesseractImpl::onWorldUnload));
        TesseractLookups.ENERGY_HANDLER_ITEM.registerFallback((s, c) -> {
            TesseractItemContext context = new ContainerItemContextWrapper(c);
            if (s.getItem() instanceof IEnergyItem energyItem && energyItem.canCreate(context)){
                return energyItem.createEnergyHandler(context);
            }
            return null;
        });
    }

    public static <T extends BlockEntity> void registerTRETile(BiFunction<T, Direction, IEnergyHandler> euFunction, BiFunction<T, Direction, EnergyContainer> rfFunction, BlockEntityType<T> type){
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            IEnergyHandler handler = euFunction.apply(blockEntity, direction);
            if (handler != null) return (EnergyStorage) handler;
            EnergyContainer node = rfFunction.apply(blockEntity, direction);
            if (node != null) return node instanceof EnergyStorage storage ? storage : node instanceof Updatable ? new FabricBlockEnergyContainer(node) : null;
            return null;
        }, type);
    }

    public static void registerTREItem(BiFunction<ItemStack, ContainerItemContext, IEnergyHandler> function, Item type){
        EnergyStorage.ITEM.registerForItems((stack, context) -> (EnergyStorage) function.apply(stack, context), type);
    }
}
