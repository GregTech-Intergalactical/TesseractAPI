package tesseract.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tesseract.Tesseract;
import tesseract.TesseractConfig;
import tesseract.api.GraphWrapper;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.forge.Provider;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.forge.wrapper.ItemStackWrapper;
import tesseract.api.gt.IEnergyItem;

@Mod(Tesseract.API_ID)
public class TesseractImpl extends Tesseract {

    public TesseractImpl() {
        Tesseract.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TesseractConfig.COMMON_SPEC);
        MinecraftForge.EVENT_BUS.addListener(this::serverStoppedEvent);
        MinecraftForge.EVENT_BUS.addListener(this::worldUnloadEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::onAttachCapabilitiesEventItemStack);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModConfigEvent);
    }

    public void onAttachCapabilitiesEventItemStack(AttachCapabilitiesEvent<ItemStack> event){
        if (event.getObject().getItem() instanceof IEnergyItem energyItem){
            TesseractItemContext context = new ItemStackWrapper(event.getObject());
            event.addCapability(new ResourceLocation(Tesseract.API_ID, "energy_items"), new Provider<>(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM, energyItem.canCreate(context) ? () -> energyItem.createEnergyHandler(context) : null));
        }
    }

    public void serverStoppedEvent(ServerStoppedEvent e) {
        firstTick.clear();
        //FE_ENERGY.clear();
        GraphWrapper.getWrappers().forEach(GraphWrapper::clear);
    }

    public void worldUnloadEvent(WorldEvent.Unload e) {
        if (!(e.getWorld() instanceof Level) || ((Level) e.getWorld()).isClientSide) return;
        //FE_ENERGY.removeWorld((World) e.getWorld());
        GraphWrapper.getWrappers().forEach(g -> g.removeWorld((Level)e.getWorld()));
        firstTick.remove(e.getWorld());
    }

    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient()) return;
        Level dim = event.world;
        if (!hadFirstTick(dim)) {
            firstTick.add(event.world);
            GraphWrapper.getWrappers().forEach(t -> t.onFirstTick(dim));
        }
        if (event.phase == TickEvent.Phase.START) {
            GraphWrapper.getWrappers().forEach(t -> t.tick(dim));
        }
        if (Tesseract.HEALTH_CHECK_TIME > 0 && event.world.getGameTime() % Tesseract.HEALTH_CHECK_TIME == 0) {
            GraphWrapper.getWrappers().forEach(GraphWrapper::healthCheck);
        }
    }

    public void onModConfigEvent(final ModConfigEvent e) {
        TesseractConfig.onModConfigEvent(e.getConfig());
    }
}
