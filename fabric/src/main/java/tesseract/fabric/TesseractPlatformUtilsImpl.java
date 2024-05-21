package tesseract.fabric;


import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import tesseract.Tesseract;
import tesseract.TesseractCapUtils;
import tesseract.TesseractPlatformUtils;
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
public class TesseractPlatformUtilsImpl implements TesseractPlatformUtils {
    @Override
    public IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        Optional<IEnergyHandler> capability = TesseractCapUtils.INSTANCE.getEnergyHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) ((TileListeners)tile).addListener(() -> invalidate.run());
            return capability.get();
        }
        return null;
    }

    @Override
    public IRFNode getRFNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
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

    @Override
    public IHeatNode getHeatNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) return null;
        Optional<IHeatHandler> capability = TesseractCapUtils.INSTANCE.getHeatHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) ((TileListeners)tile).addListener(invalidate);
            return capability.get();
        }
        return null;
    }

    @Override
    public boolean isFeCap(Class<?> cap){
        return false;
    }

    @Override
    public boolean isForge(){
        return false;
    }

    @Override
    public ConfigHandler createConfig(Config config){
        return CarbonConfig.createConfig(Tesseract.API_ID, config);
    }

    @Override
    public boolean areCapsCompatible(ItemStack a, ItemStack b){
        return true;
    }
}
