package tesseract.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.capability.TesseractGTCapability;
import tesseract.api.gt.EnergyTileWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.ItemTileWrapper;

public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
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

    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IItemHandler> h = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, capSide);
        if (h.isPresent()) {
            if (capCallback != null) h.addListener(t -> capCallback.run());
            if (h.map(t -> t instanceof IItemNode).orElse(false)) {
                return (IItemNode) h.resolve().get();
            }
            return new ItemTileWrapper(tile, h.orElse(null));
        }
        return null;
    }
}
