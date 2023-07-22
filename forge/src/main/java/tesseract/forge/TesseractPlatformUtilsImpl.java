package tesseract.forge;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.forge.energy.ForgeEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.TesseractCapUtils;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.forge.wrapper.RFWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.heat.IHeatNode;
import tesseract.api.rf.IRFNode;

import java.util.Optional;

public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractCapUtilsImpl.getEnergyHandler(tile, direction).map(e -> LazyOptional.of(() -> e)).orElse(LazyOptional.empty());
        if (capability.isPresent()) {
            if (invalidate != null )capability.addListener(o -> invalidate.run());
            return capability.resolve().get();
        }
        return null;
    }

    public static IRFNode getRFNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IEnergyStorage> capability = tile.getCapability(ForgeCapabilities.ENERGY, capSide);
        if (capability.isPresent()) {
            if (capCallback != null) capability.addListener(o -> capCallback.run());
            IEnergyStorage handler = capability.map(f -> f).orElse(null);
            if (handler instanceof ForgeEnergyContainer<?> container){
                EnergyContainer container1 = container.container().getContainer(capSide);
                if (container1 instanceof IRFNode node) return node;
            }
            return handler instanceof IRFNode node ? node : new RFWrapper(handler);
        } else {
            return null;
        }
    }

    public static IHeatNode getHeatNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) return null;
        LazyOptional<IHeatHandler> capability = tile.getCapability(TesseractCaps.HEAT_CAPABILITY, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        }
        return null;
    }

    public static boolean isFeCap(Class<?> cap){
        return cap == IEnergyStorage.class;
    }

    public static boolean isForge(){
        return true;
    }
}
