package tesseract.api.gt.fabric;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.gt.EnergyTileWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

public class GTPlatformUtilsImpl {
    public static IGTNode getNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractGTCapability.getGTEnergyHandler(tile, side);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        } else {
            LazyOptional<EnergyStorage> cap = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (cap.isPresent()) {
                EnergyTileWrapper node = new EnergyTileWrapper(tile, cap.orElse(null));
                cap.addListener(o -> invalidate.run());
                return node;
            }
        }
        return null;
    }
}
