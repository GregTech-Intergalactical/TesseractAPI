package tesseract.fabric;


import earth.terrarium.botarium.common.energy.base.EnergyAttachment;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import tesseract.TesseractCapUtils;
import tesseract.api.fabric.TileListeners;
import tesseract.api.fabric.wrapper.RFWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.heat.IHeatNode;
import tesseract.api.rf.IRFNode;
import tesseract.mixin.fabric.FabricBlockEnergyContainerAccessor;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        Optional<IEnergyHandler> capability = TesseractCapUtilsImpl.getEnergyHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) ((TileListeners)tile).addListener(() -> invalidate.run());
            return capability.get();
        }
        return null;
    }

    public static IRFNode getRFNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        if(tile instanceof EnergyAttachment attachment && attachment.getEnergyHolderType() == BlockEntity.class) {
            EnergyContainer container = attachment.getEnergyStorage(tile).getContainer(capSide);
            if (container instanceof IRFNode node) {
                if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
                return node;
            }
        }
        EnergyStorage storage = EnergyStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, capSide);
        if (storage != null){
            if (capCallback != null) ((TileListeners)tile).addListener(capCallback);
            if (storage instanceof FabricBlockEnergyContainerAccessor container && container.getContainer() instanceof IRFNode node){
                return node;
            }
            return storage instanceof IRFNode node ? node : new RFWrapper(storage);
        }
        return null;
    }

    public static IHeatNode getHeatNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) return null;
        Optional<IHeatHandler> capability = TesseractCapUtils.getHeatHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) ((TileListeners)tile).addListener(invalidate);
            return capability.get();
        }
        return null;
    }

    public static boolean isFeCap(Class<?> cap){
        return false;
    }

    public static boolean isForge(){
        return false;
    }
}
