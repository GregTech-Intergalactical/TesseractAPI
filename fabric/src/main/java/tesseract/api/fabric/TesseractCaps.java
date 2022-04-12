package tesseract.api.fabric;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import tesseract.api.gt.EnergyTransferable;
import tesseract.api.gt.IEnergyHandler;

public class TesseractCaps {
    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos) {
        return getGTEnergyHandler(level, pos, null);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return LazyOptional.empty();
        return getGTEnergyHandler(be, direction);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(BlockEntity blockEntity, Direction side){
        if (blockEntity instanceof EnergyTransferable t){
            return t.getGTEnergyHandler(side);
        }
        return LazyOptional.empty();
    }
}
