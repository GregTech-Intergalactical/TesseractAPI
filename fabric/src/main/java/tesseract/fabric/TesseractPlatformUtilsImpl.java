package tesseract.fabric;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandler;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import team.reborn.energy.api.EnergyStorage;
import tesseract.api.fabric.TesseractCaps;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fabric.wrapper.EnergyTileWrapper;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.fabric.wrapper.ItemTileWrapper;

public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractCaps.getGTEnergyHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        } else if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")){
            EnergyStorage storage = EnergyStorage.SIDED.find(level, BlockPos.of(pos), level.getBlockState(BlockPos.of(pos)), tile, direction);
            if (storage != null) {
                return new EnergyTileWrapper(tile, storage);
            }
        }
        return null;
    }

    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) {
            return null;
        }
        LazyOptional<IItemHandler> h = TransferUtil.getItemHandler(tile, capSide);
        if (h.isPresent()) {
            if (capCallback != null) h.addListener(t -> capCallback.run());
            if (h.map(t -> t instanceof IItemNode).orElse(false)) {
                return (IItemNode) h.resolve().get();
            }
            return new ItemTileWrapper(tile, h.orElse(null));
        }
        return null;
    }

    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        return null;
    }

    public static ResourceLocation getFluidId(Fluid fluid){
        return Registry.FLUID.getKey(fluid);
    }

    public static int getFluidTemperature(Fluid fluid){
        return 0;
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return false;
    }
}
