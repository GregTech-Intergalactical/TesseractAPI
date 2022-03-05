package tesseract.api.gt.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.gt.EnergyTileWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

public class GTPlatformUtilsImpl {
    public static IGTNode getNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = tile.getCapability(TesseractGTCapability.ENERGY_HANDLER_CAPABILITY, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        } else {
            LazyOptional<IEnergyStorage> cap = tile.getCapability(CapabilityEnergy.ENERGY, direction);
            if (cap.isPresent()) {
                EnergyTileWrapper node = new EnergyTileWrapper(tile, cap.orElse(null));
                cap.addListener(o -> invalidate.run());
                return node;
            }
        }
        return null;
    }
}
